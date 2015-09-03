package com.widespace.wisper;

import com.widespace.wisper.controller.RPCController;
import com.widespace.wisper.controller.RPCControllerCallback;
import com.widespace.wisper.messagetype.RPCNotification;
import com.widespace.wisper.messagetype.RPCRequest;
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

    private RPCController rpcController;
    private RPCControllerCallback callbackMock;

    @Before
    public void setUp() throws Exception
    {
        callbackMock = mock(RPCControllerCallback.class);
        rpcController = new RPCController(callbackMock);
    }


    @Test
    public void testCallBackRequestReceivedIsCalled() throws Exception
    {
        rpcController.handle(SAMPLE_INSTANCE_METHOD_CALL_REQUEST);
        verify(callbackMock).rpcControllerReceivedRequest(any(RPCRequest.class));
    }

    @Test
    public void testCallBackNotificationReceivedIsCalled() throws Exception
    {
        rpcController.handle(SAMPLE_NOTIFICATION);
        verify(callbackMock).rpcControllerReceivedNotification(any(RPCNotification.class));
    }

}
