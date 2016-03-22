package com.widespace.wisper.proxy;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.route.Channel;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class RemoteGatewayTest
{

    @Test
    public void remoteGatewayIsAWisperObject() throws Exception
    {
        RemoteGateway remoteGateway = new RemoteGateway(mock(Channel.class));
        assertThat(remoteGateway, is(instanceOf(Wisper.class)));
    }

    @Test
    public void remoteGatewayCreationMakesAGatewayRouter() throws Exception
    {
        RemoteGateway remoteGateway = new RemoteGateway(mock(Channel.class));
        assertThat(remoteGateway.getGatewayRouter(), is(notNullValue()));
    }

    @Test
    public void sendMessagesToTheChannel() throws Exception
    {
        TestChannel testChannel = new TestChannel();
        RemoteGateway remoteGateway = new RemoteGateway(testChannel);

        remoteGateway.getGatewayRouter().getGatewayCallback().gatewayGeneratedMessage("a message");

        assertThat(testChannel.isSendMessageCalled(), is(true));
    }

    @Test
    public void receivesMessagesFromTheChannel() throws Exception
    {
        TestChannel testChannel = new TestChannel();
        RemoteGateway remoteGateway = new RemoteGateway(testChannel);




    }

    private class TestChannel extends Channel
    {
        private boolean sendMessageCalled = false;

        @Override
        public void sendMessage(String message)
        {
            sendMessageCalled = true;
        }

        public boolean isSendMessageCalled()
        {
            return sendMessageCalled;
        }
    }
}

