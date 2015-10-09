package com.widespace.wisper;

import com.widespace.wisper.messagetype.MessageFactory;
import com.widespace.wisper.messagetype.RPCMessageType;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class MessageFactoryTest
{

    private MessageFactory messageFactory;

    @Before
    public void setUp() throws Exception
    {
        messageFactory = new MessageFactory();
    }

    @Test
    public void canDetermineRequestType() throws Exception
    {
        JSONObject jsonRequest = new JSONObject("{\"id\":\"1234\", \"method\":\"method_name\", \"params\":[\"index_1\", \"index_2\"] }");
        assertThat(messageFactory.determineMessageType(jsonRequest), is(RPCMessageType.REQUEST));
    }

    @Test
    public void canDetermineResponseType() throws Exception
    {
        JSONObject jsonResponse = new JSONObject("{\"id\":\"1234\", \"result\":\"the_result\"}");
        assertThat(messageFactory.determineMessageType(jsonResponse), is(RPCMessageType.RESPONSE));
    }

    @Test
    public void canDetermineRpcErrorType() throws Exception
    {
        JSONObject jsonError = new JSONObject("{\"id\":\"1234\", \"error\": { \"domain\":\"error_domain\", \"code\":\"error_code\", \"description\":\"error_desc\" } } ");
        assertThat(messageFactory.determineMessageType(jsonError), is(RPCMessageType.ERROR));
    }

    @Test
    public void canDetermineNotificationType() throws Exception
    {
        JSONObject jsonNotif = new JSONObject("{\"method\":\"wisp.example.notification\", \"params\":[\"index_1\", \"index_2\"] }");
        assertThat(messageFactory.determineMessageType(jsonNotif), is(RPCMessageType.NOTIFICATION));
    }

    @Test
    public void willGiveUnknownForUnknownMessageType() throws Exception
    {
        JSONObject jsonUnknown = new JSONObject("{\"key\":\"wisp.example.notification\", \"key2\":[\"index_1\", \"index_2\"] }");
        assertThat(messageFactory.determineMessageType(jsonUnknown), is(RPCMessageType.UNKNOWN));
    }

    @Test
    public void canHandleNull() throws Exception
    {
        assertThat(messageFactory.determineMessageType(null), is(RPCMessageType.UNKNOWN));
    }

}