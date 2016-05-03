package com.widespace.wisper.base;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class WisperRemoteObjectTest
{

    @Test
    public void testInstanceIdentifier() throws Exception
    {
        WisperRemoteObject obj = new WisperRemoteObject("Foo", new DoNothingChannel());

        assertThat(obj.getInstanceIdentifier(), is(equalTo(null)));

        obj.setInstanceIdentifier("foo1");

        assertThat(obj.getInstanceIdentifier(), is(equalTo("foo1")));
    }

    @Test
    public void testCallInstanceMethod() throws Exception
    {
        LoggingChannel channel = new LoggingChannel();
        WisperRemoteObject obj = new WisperRemoteObject("Foo", channel);

        // Call a method before instance identifier has been set
        obj.callInstanceMethod("bar", new Object[]{1, 2, 3});

        // Assert that no message has been sent
        assertThat(channel.messages.size(), is(equalTo(0)));

        // Set the identifier
        obj.setInstanceIdentifier("foo2");

        Request expected = new Request("Foo:bar", new Object[]{"foo2", 1, 2, 3}).withResponseBlock(WisperRemoteObject.DoNothingResponseBlock);

        // Assert a message has been sent and that it equals the expected one
        assertThat(channel.messages.size(), is(equalTo(1)));
        assertThat((Request) channel.messages.get(0), is(equalTo(expected)));

        // Call the method a second time
        obj.callInstanceMethod("bar", new Object[]{1, 2, 3});

        // Assert that another message is sent immediately
        assertThat(channel.messages.size(), is(equalTo(2)));
        assertThat((Request) channel.messages.get(1), is(equalTo(expected)));
    }

    @Test
    public void testCallStaticMethod() throws Exception
    {
        LoggingChannel channel = new LoggingChannel();
        WisperRemoteObject obj = new WisperRemoteObject("Foo", channel);

        obj.callStaticMethod("foo", new Object[]{1});

        obj.callStaticMethod("foo", new Object[]{1}, new CompletionBlock()
        {
            @Override
            public void perform(@Nullable Object result, @Nullable RPCErrorMessage error)
            {
                // Do nothing
            }
        });

        for (AbstractMessage message : channel.messages)
        {
            Request request = (Request) message;

            // Just call the response block to see that we get complete code coverage
            request.getResponseBlock().perform(new Response(), null);
        }
    }

    @Test
    public void testSendInstanceEvent() throws Exception
    {
        LoggingChannel channel = new LoggingChannel();
        WisperRemoteObject obj = new WisperRemoteObject("Foo", channel);

        obj.sendInstanceEvent("baz", 1);

        // Assert that no message has been sent
        assertThat(channel.messages.size(), is(equalTo(0)));

        // Set the identifier
        obj.setInstanceIdentifier("foo2");

        Event expected = new Event("Foo:!", "foo2", "baz", 1);

        assertThat(channel.messages.size(), is(equalTo(1)));
        assertThat((Event) channel.messages.get(0), is(equalTo(expected)));

        obj.sendInstanceEvent("baz", 1);

        assertThat(channel.messages.size(), is(equalTo(2)));
        assertThat((Event) channel.messages.get(1), is(equalTo(expected)));

    }

    @Test
    public void testSendStaticEvent() throws Exception
    {
        LoggingChannel channel = new LoggingChannel();
        WisperRemoteObject obj = new WisperRemoteObject("Foo", channel);

        obj.sendStaticEvent("create", null);

        Event expected = new Event("Foo!", "create", null);

        assertThat(channel.messages.size(), is(equalTo(1)));
        assertThat((Event) channel.messages.get(0), is(equalTo(expected)));
    }

    private class LoggingChannel implements Channel
    {
        public List<AbstractMessage> messages = new ArrayList<AbstractMessage>();

        @Override
        public void sendMessage(AbstractMessage message)
        {
            messages.add(message);
        }
    }

    private class DoNothingChannel implements Channel
    {
        @Override
        public void sendMessage(AbstractMessage message)
        {
            // do nothing
        }
    }
}
