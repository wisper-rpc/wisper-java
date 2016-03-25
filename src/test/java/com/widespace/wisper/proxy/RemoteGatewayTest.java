package com.widespace.wisper.proxy;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.route.Channel;

import com.widespace.wisper.route.ClassRouter;
import com.widespace.wisper.route.GatewayRouter;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


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
        Gateway gateway = mock(Gateway.class);
        GatewayRouter gatewayRouter = new GatewayRouter(gateway);
        //make a remote gateway
        RemoteGateway remoteGateway = new RemoteGateway(gatewayRouter, testChannel);

        //channel receives a message
        String aMessage = "a message";
        testChannel.receiveMessage(aMessage);

        //gateway must receuve it too
        verify(gateway).handleMessage(aMessage);
    }


    @Test
    public void testSendingNestedRequests() throws Exception
    {
        GatewayRouter controllerGatewayRouter = new GatewayRouter(new Gateway(mock(GatewayCallback.class)));
        ClassRouter remoteGatewayClassRouter = new ClassRouter(RemoteGateway.class);
        controllerGatewayRouter.exposeRoute("Gateway", remoteGatewayClassRouter);

        final RemoteGateway remoteGateway = new RemoteGateway(new TestChannel());
        WisperInstanceModel remoteGatewayWisperInstanceModel = remoteGatewayClassRouter.addInstance(remoteGateway);

        final Request inner_request = new Request().withMethodName("some.path:method");
        inner_request.setIdentifier("inner_001");

        Object[] params =  new Object[]{remoteGatewayWisperInstanceModel.getInstanceIdentifier(), inner_request.toJsonString()};
        final Request outer_request=new Request().withMethodName("Gateway:sendMessage").withParams(params);
        outer_request.setIdentifier("outer_001");
        outer_request.setResponseBlock(new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                assertThat(error, is(nullValue()));
                assertThat(response, is(notNullValue()));
                assertThat(response.getIdentifier(), is(outer_request.getIdentifier()));
            }
        });

        controllerGatewayRouter.getGateway().handleMessage(outer_request);
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

