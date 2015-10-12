package com.widespace.wisper;

import com.widespace.wisper.controller.RPCController;
import com.widespace.wisper.controller.RPCControllerCallback;
import com.widespace.wisper.messagetype.RPCNotification;
import com.widespace.wisper.messagetype.RPCRequest;
import com.widespace.wisper.proxy.RPCProxy;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RPCProxyTests
{
    private static final String SAMPLE_INSTANCE_ID = "1234";
    private RPCProxy proxy;

    @Before
    public void setUp() throws Exception
    {
        proxy = new RPCProxy();
    }

    @Test
    public void testMapNameWorks() throws Exception
    {
        proxy.setMapName("TEST_MAP");
        assertEquals("TEST_MAP", proxy.getMapName());
    }

    @Test
    public void testReceiverWorks() throws Exception
    {
        RPCController rpcController = mock(RPCController.class);
        proxy.setReceiver(rpcController);
        assertEquals(rpcController, proxy.getReceiver());
    }

    @Test
    public void testReceiverMapWorks() throws Exception
    {
        proxy.setReceiverMapName("SOME_TEST_REC_MAP");
        assertEquals("SOME_TEST_REC_MAP", proxy.getReceiverMapName());
    }

    @Test
    public void testHandlingRequestCallsReceiverCorrectly() throws Exception
    {
        proxy.setReceiverMapName("my.receiver.mapname");
        RPCController receiverMock = mock(RPCController.class);
        proxy.setReceiver(receiverMock);
        proxy.setMapName("proxy.map.name");
        RPCRequest sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"proxy.map.name.wisp.ai.MyRPCTestObject:~\", \"params\" : [\"" + SAMPLE_INSTANCE_ID + "\"], \"id\": \"abcd5\" }"),  null);

        String expected = "{\"method\":\"my.receiver.mapname.wisp.ai.MyRPCTestObject:~\",\"params\":[\"1234\"],\"id\":\"abcd5\"}";
        proxy.handleRequest(sampleRequest);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(receiverMock).handle(argument.capture());
        JSONAssert.assertEquals(expected, argument.getValue(), false);
    }

    @Test
    public void testHandlingNotificationsCallsReceiverCorrectly() throws Exception
    {
        proxy.setReceiverMapName("my.receiver.mapname");
        RPCController receiverMock = mock(RPCController.class);
        proxy.setReceiver(receiverMock);
        proxy.setMapName("proxy.map.name");
        RPCNotification sampleNotification = new RPCNotification(new JSONObject("{ \"method\" : \"proxy.map.name.wisp.ai.MyRPCTestObject:~\", \"params\" : [\"" + SAMPLE_INSTANCE_ID + "\"] }"), mock(RPCControllerCallback.class));

        String expected = "{\"method\":\"my.receiver.mapname.wisp.ai.MyRPCTestObject:~\",\"params\":[\"1234\"]}";
        proxy.handleNotification(sampleNotification);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(receiverMock).handle(argument.capture());
        JSONAssert.assertEquals(expected, argument.getValue(), false);
    }
}