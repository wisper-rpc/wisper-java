package com.widespace.wisper;

import com.widespace.wisper.controller.RemoteObjectCall;
import com.widespace.wisper.route.WisperCallType;
import com.widespace.wisper.messagetype.Request;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class RemoteObjectCallTests
{

    public static final String SAMPLE_INSTANCE_METHOD_CALL = "{\"method\":\"wisp.ai.TestObject:sampleMethodName\", \"params\":[\"sample_instance_identifier\"],\"id\":\"abcd1\"}";
    private RemoteObjectCall remoteObjectCall;
    private Request sampleRequest;

    @Before
    public void setUp() throws Exception
    {
        remoteObjectCall = null;
        sampleRequest = null;
    }

    @Test
    public void testRequestInitializesProperly() throws Exception
    {
        sampleRequest = new Request(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(sampleRequest, remoteObjectCall.getRequest());
    }

    @Test
    public void testInstanceIdentifierCanBeDetermined() throws Exception
    {
        sampleRequest = new Request(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals("sample_instance_identifier", remoteObjectCall.getInstanceIdentifier());
    }

    @Test
    public void testNullInstanceIdentifierInStaticMethodCall() throws JSONException
    {
        sampleRequest = new Request(new JSONObject("{ \"method\" : \"wisp.ai.TestObject.sampleMethodName\", \"params\" : [{\"status\":\"OK\"}], \"id\": \"abcd4\" }"),
                null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertNull(remoteObjectCall.getInstanceIdentifier());
    }

    @Test
    public void testMethodName() throws Exception
    {
        sampleRequest = new Request(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals("sampleMethodName", remoteObjectCall.getMethodName());
    }

    @Test
    public void testFullMethodName() throws Exception
    {
        sampleRequest = new Request(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals("wisp.ai.TestObject:sampleMethodName", remoteObjectCall.getFullMethodName());
    }

    @Test
    public void testCallTypeForInstanceMethodCalls() throws Exception
    {
        sampleRequest = new Request(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.INSTANCE, remoteObjectCall.getCallType());
    }

    @Test
    public void testCallTypeForStaticMethodCalls() throws Exception
    {
        sampleRequest = new Request(new JSONObject("{\"method\":\"wisp.ai.TestObject.sampleMethodName\", \"params\":[],\"id\":\"abcd1\"}"), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.STATIC, remoteObjectCall.getCallType());
    }

    @Test
    public void testCallTypeForConstructors() throws Exception
    {
        sampleRequest = new Request(new JSONObject("{ \"method\" : \"ClassName~\", \"params\" : [], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.CREATE, remoteObjectCall.getCallType());
    }

    @Test
    public void testCallTypeForDestructors() throws Exception
    {
        sampleRequest = new Request(new JSONObject("{ \"method\" : \"ClassName:~\", \"params\" : [\"some_random_instance_id\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.DESTROY, remoteObjectCall.getCallType());
    }

    @Test
    public void testCallTypeForInstanceEvents() throws Exception
    {
        sampleRequest = new Request(new JSONObject("{ \"method\" : \"ClassName:!\", \"params\" : [\"some_random_event_name\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.INSTANCE_EVENT, remoteObjectCall.getCallType());
    }

    @Test
    public void testCallTypeForStaticEvents() throws Exception
    {
        sampleRequest = new Request(new JSONObject("{ \"method\" : \"ClassName!\", \"params\" : [\"some_random_event_name\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RemoteObjectCall(sampleRequest);
        assertEquals(WisperCallType.STATIC_EVENT, remoteObjectCall.getCallType());
    }

}
