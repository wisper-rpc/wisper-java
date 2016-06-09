package com.widespace.wisper;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
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
        Gateway gateway = mock(Gateway.class);
        proxy.setReceiver(gateway);
        assertEquals(gateway, proxy.getReceiver());
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
        Gateway receiverMock = mock(Gateway.class);
        proxy.setReceiver(receiverMock);
        proxy.setMapName("proxy.map.name");
        Request sampleRequest = new Request(new JSONObject("{ \"method\" : \"proxy.map.name.wisp.ai.MyWisperTestObject:~\", \"params\" : [\"" + SAMPLE_INSTANCE_ID + "\"], \"id\": \"abcd5\" }"), null);

        String expected = "{\"method\":\"my.receiver.mapname.wisp.ai.MyWisperTestObject:~\",\"params\":[\"1234\"],\"id\":\"abcd5\"}";
        proxy.handleRequest(sampleRequest);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(receiverMock).handleMessage(argument.capture());
        JSONAssert.assertEquals(expected, argument.getValue(), false);
    }

    @Test
    public void testHandlingNotificationsCallsReceiverCorrectly() throws Exception
    {
        proxy.setReceiverMapName("my.receiver.mapname");
        Gateway receiverMock = mock(Gateway.class);
        proxy.setReceiver(receiverMock);
        proxy.setMapName("proxy.map.name");

        String expected = "{\"method\":\"my.receiver.mapname.wisp.ai.MyWisperTestObject:~\",\"params\":[\"1234\"]}";
        proxy.handleNotification(new Notification("proxy.map.name.wisp.ai.MyWisperTestObject:~", new Object[]{SAMPLE_INSTANCE_ID}));

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(receiverMock).handleMessage(argument.capture());
        JSONAssert.assertEquals(expected, argument.getValue(), false);
    }
}
