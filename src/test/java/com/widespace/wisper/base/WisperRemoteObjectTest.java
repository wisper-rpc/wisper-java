package com.widespace.wisper.base;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.route.EventRouter;
import com.widespace.wisper.route.FunctionRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.Router;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WisperRemoteObjectTest
{

    @Test
    public void testInstanceIdentifier() throws Exception
    {
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", mock(GatewayRouter.class));

        assertThat(obj.getInstanceIdentifier(), is(equalTo(null)));

        obj.setInstanceIdentifier("foo1");

        assertThat(obj.getInstanceIdentifier(), is(equalTo("foo1")));
    }


    @Test
    public void testCallInstanceMethod() throws Exception
    {
        StubbedGatewayRouter channel = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", channel);

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
        StubbedGatewayRouter channel = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", channel);

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
        StubbedGatewayRouter channel = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", channel);

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
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        obj.sendStaticEvent("create", null);

        Event expected = new Event("Foo!", "create", null);

        assertThat(router.messages.size(), is(equalTo(1)));
        assertThat((Event) router.messages.get(0), is(equalTo(expected)));
    }


    @Test
    public void testWhenCreatedGatewayRouter_canRouteMapName() throws Exception
    {
        StubbedGatewayRouter gatewayRouter = new StubbedGatewayRouter();
        WisperRemoteObject object = new MyWisperRemoteObject("whatever.some.thing", gatewayRouter);

        assertThat(gatewayRouter.getRoutes().containsKey("whatever"), is(true));
        Router router1 = gatewayRouter.getRoutes().get("whatever");

        assertThat(router1.getRoutes().containsKey("some"), is(true));
        Router router2 = router1.getRoutes().get("some");

        assertThat(router2.getRoutes().containsKey("thing"), is(true));

        assertThat(gatewayRouter.hasRoute("whatever.some.thing"), is(true));
    }

    @Test
    public void testWhenRemoteObjectCreated_EventRouterIsAddedToGatewayRouter() throws Exception
    {
        StubbedGatewayRouter gatewayRouter = new StubbedGatewayRouter();
        WisperRemoteObject object = new MyWisperRemoteObject("a.b.c", gatewayRouter);

        assertThat(gatewayRouter.hasRoute("a.b.c"), is(true));

        Router finalRouter = gatewayRouter.getRoutes().get("a").getRoutes().get("b").getRoutes().get("c");
        assertThat(finalRouter, is(instanceOf(EventRouter.class)));
    }

    //region Remote Object Events
    //==================================================================

    @Test
    public void testWisperRemoteObject_handlesInstanceEvent() throws Exception
    {
        String instanceId = "testIdentifier";
        StubbedGatewayRouter stubbedGatewayRouter = new StubbedGatewayRouter();
        MyWisperRemoteObject remoteObject = new MyWisperRemoteObject("a.b.c",stubbedGatewayRouter);
        remoteObject.registerInstanceOnEventRouter(instanceId);
        Event instanceEvent = new WisperEventBuilder().withInstanceIdentifier(instanceId).withMethodName("a.b.c").buildInstanceEvent();
        stubbedGatewayRouter.routeMessage(instanceEvent,"a.b.c");

        assertThat(remoteObject.recievedEvent, is(notNullValue()));
        assertThat(remoteObject.recievedEvent.getIdentifier(),is(equalTo(instanceEvent.getIdentifier())));
        assertThat(remoteObject.recievedEvent.getInstanceIdentifier(),is(equalTo(instanceEvent.getInstanceIdentifier())));
    }


    //region Utilities
    //==================================================================


    private class StubbedGatewayRouter extends GatewayRouter
    {
        public List<AbstractMessage> messages = new ArrayList<AbstractMessage>();
        public Gateway gateway = new Gateway(mock(GatewayCallback.class))
        {
            @Override
            public void sendMessage(AbstractMessage message)
            {
                messages.add(message);
            }
        };

        @Override
        public Gateway getGateway()
        {
            return gateway;
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
