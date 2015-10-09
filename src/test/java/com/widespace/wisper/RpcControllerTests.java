package com.widespace.wisper;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Ehssan Hoorvash on 13/06/14.
 */
public class RpcControllerTests
{
    public static final String SAMPLE_INSTANCE_METHOD_CALL_REQUEST = "{\"method\":\"wisp.ai.TestObject:sampleMethodName\", \"params\":[\"sample_instance_identifier\"],\"id\":\"abcd1\"}";
    private static final String SAMPLE_NOTIFICATION = "{ \"method\" : \"swipeTo\", \"params\" : [\"face\", 2] }";

    private Gateway gateway;
    private GatewayCallback callbackMock;

    @Before
    public void setUp() throws Exception
    {
        callbackMock = mock(GatewayCallback.class);
        gateway = new Gateway(callbackMock);
    }


    @Test
    public void testCallBackRequestReceivedIsCalled() throws Exception
    {
        gateway.handle(SAMPLE_INSTANCE_METHOD_CALL_REQUEST);
        verify(callbackMock).gatewayReceivedMessage(any(Request.class));
    }

    @Test
    public void testCallBackNotificationReceivedIsCalled() throws Exception
    {
        gateway.handle(SAMPLE_NOTIFICATION);
        verify(callbackMock).gatewayReceivedMessage(any(Notification.class));
    }

}
