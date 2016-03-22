package com.widespace.wisper.route;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.Request;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ChannelTest
{
    @Test
    public void givenNoGateway_MessagesAreNotForwarded() throws Exception
    {
        Channel channel = spy(new TestChannel());
        assertThat(channel.getGateway(), is(nullValue()));

        channel.receiveMessage(new Request().toJsonString());
        //of exception is thrown, test fails.
    }

    @Test
    public void givenGateway_MessagesAreForwarded() throws Exception
    {
        Channel channel = new TestChannel();
        Gateway gateway = mock(Gateway.class);
        channel.setGateway(gateway);
        Request mesage = new Request();

        channel.receiveMessage(mesage.toJsonString());
        verify(gateway).handleMessage(mesage.toJsonString());
    }

    private class TestChannel extends Channel
    {
        @Override
        public void sendMessage(String message)
        {

        }
    }
}