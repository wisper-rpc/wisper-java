package com.widespace.wisper;

import com.widespace.wisper.controller.RPCRemoteObjectCall;
import com.widespace.wisper.controller.RPCRemoteObjectCallType;
import com.widespace.wisper.messagetype.RPCRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class RemoteObjectCallTests
{

    public static final String SAMPLE_INSTANCE_METHOD_CALL = "{\"method\":\"wisp.ai.TestObject:sampleMethodName\", \"params\":[\"sample_instance_identifier\"],\"id\":\"abcd1\"}";
    private RPCRemoteObjectCall remoteObjectCall;
    private RPCRequest sampleRequest;

    @Before
    public void setUp() throws Exception
    {
        remoteObjectCall = null;
        sampleRequest = null;
    }

    @Test
    public void testRequestInitializesProperly() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(sampleRequest, remoteObjectCall.getRequest());
    }

    public void testInstanceIdentifierCanBeDetermined() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals("sample_instance_identifier", remoteObjectCall.getInstanceIdentifier());
    }

    public void testNullInstanceIdentifierInStaticMethodCall() throws JSONException
    {
        sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.TestObject.sampleMethodName\", \"params\" : [{\"status\":\"OK\"}], \"id\": \"abcd4\" }"),
                null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertNull(remoteObjectCall.getInstanceIdentifier());
    }

    public void testMethodName() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals("sampleMethodName", remoteObjectCall.getMethodName());
    }

    public void testFullMethodName() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals("wisp.ai.TestObject:sampleMethodName", remoteObjectCall.getFullMethodName());
    }

    public void testCallTypeForInstanceMethodCalls() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject(SAMPLE_INSTANCE_METHOD_CALL), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.INSTANCE, remoteObjectCall.getCallType());
    }

    public void testCallTypeForStaticMethodCalls() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject("{\"method\":\"wisp.ai.TestObject.sampleMethodName\", \"params\":[],\"id\":\"abcd1\"}"), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.STATIC, remoteObjectCall.getCallType());
    }

    public void testCallTypeForConstructors() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"ClassName~\", \"params\" : [], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.CREATE, remoteObjectCall.getCallType());
    }

    public void testCallTypeForDestructors() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"ClassName:~\", \"params\" : [\"some_random_instance_id\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.DESTROY, remoteObjectCall.getCallType());
    }

    public void testCallTypeForInstanceEvents() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"ClassName:!\", \"params\" : [\"some_random_event_name\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.INSTANCE_EVENT, remoteObjectCall.getCallType());
    }

    public void testCallTypeForStaticEvents() throws Exception
    {
        sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"ClassName!\", \"params\" : [\"some_random_event_name\"], \"id\": \"abcd4\" }"), null);
        remoteObjectCall = new RPCRemoteObjectCall(sampleRequest);
        assertEquals(RPCRemoteObjectCallType.STATIC_EVENT, remoteObjectCall.getCallType());
    }

}
