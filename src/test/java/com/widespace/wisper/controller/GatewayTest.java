package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class GatewayTest
{

    private GatewayCallback callbackMock;
    private Gateway gateway;
    private final String VALID_REQUEST = "{ \"method\" : \"dummy.dummy.fake\", \"params\" : [\"Hello World!\"], \"id\": \"abcd1\"}";
    private final String INVALID_REQUEST = "{ \"unspecified\" : \"dummy.dummy.fake\", \"params\" : [\"Hello World!\"], \"id\": \"abcd1\"}";

    @Before
    public void setUp() throws Exception
    {
        callbackMock = mock(GatewayCallback.class);
        gateway = new Gateway(callbackMock);
    }

    @Test
    public void testGatewayAcceptsExtras() throws Exception
    {
        Object value = new Object();
        gateway.setExtra("EXTRA_KEY", value);
        assertThat(1, is(equalTo(gateway.getExtras().size())));
        assertThat(value, is(equalTo(gateway.getExtra("EXTRA_KEY"))));
    }

    @Test
    public void testGatewayGeneratesUniqueIdForRequests() throws Exception
    {
        String identifier = gateway.uniqueRequestIdentifier();
        assertThat("WISPER-ANDROID-", is(equalTo(identifier.substring(0, 15))));
    }

    @Test
    public void testGatewayHandlesStringMessages() throws Exception
    {
        gateway.handleMessage(VALID_REQUEST);
        verify(callbackMock).gatewayReceivedMessage(any(AbstractMessage.class));
    }

    @Test
    public void testGatewayReturnsErrorOnMalformedJsonMessages() throws Exception
    {
        gateway.handleMessage("{this:\"is a malformed json}");
        verify(callbackMock, never()).gatewayReceivedMessage(any(AbstractMessage.class));
        verify(callbackMock).gatewayGeneratedMessage(anyString());
    }

    @Test
    public void testGatewayReturnsErrorOnInvalidWisperMessages() throws Exception
    {
        gateway.handleMessage(INVALID_REQUEST);
        verify(callbackMock, never()).gatewayReceivedMessage(any(AbstractMessage.class));
        verify(callbackMock).gatewayGeneratedMessage(anyString());
    }


    @Test
    public void testSendMessageGeneratesOutput() throws Exception
    {
        gateway.sendMessage(VALID_REQUEST);
        verify(callbackMock).gatewayGeneratedMessage(VALID_REQUEST);
    }

    @Test
    public void testGatewayHandlesWisperMessages() throws Exception
    {
        Request request = new Request(new JSONObject(VALID_REQUEST));
        gateway.handleMessage(request);
        verify(callbackMock).gatewayReceivedMessage(request);
    }

    @After
    public void tearDown() throws Exception
    {
        callbackMock = null;
        gateway = null;
    }
}