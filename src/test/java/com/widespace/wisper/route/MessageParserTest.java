package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class MessageParserTest
{
    @Test
    public void givenInstanceCallMessage_CanDetermineCallType() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c:m");
        WisperCallType callType = MessageParser.getCallType(request);
        assertThat(callType, is(WisperCallType.INSTANCE_METHOD));
    }

    @Test
    public void givenStaticCallMessage_CanDetermineCallType() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m");
        WisperCallType callType = MessageParser.getCallType(request);
        assertThat(callType, is(WisperCallType.STATIC_METHOD));
    }

    @Test
    public void givenStaticEventMessage_CanDetermineCallType() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m!");
        WisperCallType callType = MessageParser.getCallType(request);
        assertThat(callType, is(WisperCallType.STATIC_EVENT));
    }

    @Test
    public void givenInstanceEventMessage_CanDetermineCallType() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c:m!");
        WisperCallType callType = MessageParser.getCallType(request);
        assertThat(callType, is(WisperCallType.INSTANCE_EVENT));
    }



    @Test
    public void givenStaticMethodCallMessage_methodNameParses() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m");
        String methodName = MessageParser.getMethodName(request);
        assertThat(methodName, is("m"));
    }

    @Test
    public void givenInstanceMethodCallMessage_methodNameParses() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c:m");
        String methodName = MessageParser.getMethodName(request);
        assertThat(methodName, is("m"));
    }

    @Test
    public void givenStaticEventMessage_methodNameParses() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m!");
        String methodName = MessageParser.getMethodName(request);
        assertThat(methodName, is(nullValue()));
    }

    @Test
    public void givenInstanceEventMessage_methodNameParses() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c:m!");
        String methodName = MessageParser.getMethodName(request);
        assertThat(methodName, is(nullValue()));
    }

    @Test
    public void givenMessage_fullMethodNameParses() throws Exception
    {
        String fullMethodName = "a.b.c:m!";
        Request request = new Request().withMethodName(fullMethodName);
        String methodName = MessageParser.getFullMethodName(request);
        assertThat(methodName, is(equalTo(fullMethodName)));
    }

    @Test
    public void givenMessageWithNoParams_returnsNoParams() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m");
        Object[] params = MessageParser.getParams(request);
        assertThat(params, is(nullValue()));
    }

    @Test
    public void givenMessageWithParams_returnsParams() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m");
        Object[] sampleParams = {"param1", "param2"};
        request.setParams(sampleParams);
        Object[] params = MessageParser.getParams(request);
        assertThat(params, is(equalTo(sampleParams)));
        assertThat((String) params[0], is(equalTo("param1")));
        assertThat((String) params[1], is(equalTo("param2")));
    }

    @Test
    public void givenStaticCallMessage_canGetClassName() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c.m");
        String className = MessageParser.getClassName(request);
        assertThat(className, is("a.b.c"));
    }

    @Test
    public void givenInstanceCallMessage_canGetClassName() throws Exception
    {
        Request request = new Request().withMethodName("a.b.c:m");
        String className = MessageParser.getClassName(request);
        assertThat(className, is("a.b.c"));
    }


}