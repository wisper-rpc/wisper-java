package com.widespace.wisper;

import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.controller.RPCControllerCallback;
import com.widespace.wisper.controller.RPCRemoteObjectController;
import com.widespace.wisper.messagetype.RPCNotification;
import com.widespace.wisper.messagetype.RPCRequest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Ehssan Hoorvash on 13/06/14.
 */
public class RpcRemoteObjectControllerTests
{
    private RPCRemoteObjectController remoteObjectController;
    private RPCControllerCallback callBackMock;


    @Before
    public void setUp() throws Exception
    {
        callBackMock = mock(RPCControllerCallback.class);
        remoteObjectController = new RPCRemoteObjectController(callBackMock);
    }

    @Test
    public void testClassConversionForNonRegisteredClassReturnsNull() throws Exception
    {
        RPCClass rpcClassForClass = remoteObjectController.getRpcClassForClass(String.class);
        assertNull(rpcClassForClass);
    }

    @Test
    public void testFlushesInstancesProperly() throws Exception
    {
        remoteObjectController.flushInstances();
        assertEquals(remoteObjectController.getInstanceMap().size(), 0);
    }

    @Test
    public void testRegisteringClassesWorks() throws Exception
    {
        RPCClass rpcClass = MyRPCTestObject.registerRpcClass();
        remoteObjectController.registerClass(rpcClass);
        assertEquals(remoteObjectController.getRpcClassForClass(MyRPCTestObject.class), rpcClass);
    }

    @Test
    public void testCanHandleRequestForInstanceCreation() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());
    }

    public void testCanHandleRequestForInstanceDestruction() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //destruct
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        RPCRequest sampleRequest = new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:~\", \"params\" : [\"" + instanceIdentifier + "\"], \"id\": \"abcd5\" }"), null);
        remoteObjectController.handle(sampleRequest.toJsonString());

        assertTrue(remoteObjectController.getInstanceMap().isEmpty());
    }

    public void testCallingInstanceMethodsWorks() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //Call instance method
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        RPCRequest instanceMethodRequest = new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:" + MyRPCTestObject.TEST_INSTANCE_METHOD_MAPPING_NAME + "\", \"params\" : [\"" + instanceIdentifier + "\", \"something_else\"], \"id\": \"abcd4\" }"), null);
        remoteObjectController.handle(instanceMethodRequest.toJsonString());

        assertEquals(MyRPCTestObject.TEST_INSTANCE_METHOD_MAPPING_NAME, MyRPCTestObject.getLastMethodCalled());
    }

    public void testCallingStaticMethodWorks() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //Call instance method
        RPCRequest staticMethodRequest = new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject." + MyRPCTestObject.TEST_STATIC_METHOD_MAPPING_NAME + "\", \"params\" : [\"some_string_param\"], \"id\": \"abcd4\" }"), null);
        remoteObjectController.handle(staticMethodRequest.toJsonString());

        assertEquals(MyRPCTestObject.TEST_STATIC_METHOD_MAPPING_NAME, MyRPCTestObject.getLastMethodCalled());
    }

    public void testRPCPropertiesAreSetWithInstanceEvents() throws Exception
    {
        registerAndCreateTestObject();
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        RPCRequest instanceEventRequest = new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:!\", \"params\" : [\"" + instanceIdentifier + "\", \"" + MyRPCTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"new_prop_value\"] }"), null);
        remoteObjectController.handle(instanceEventRequest.toJsonString());

        assertEquals("new_prop_value", MyRPCTestObject.propertyValue);
    }

    public void testCallingPassByReferenceWorksOnInstanceMethods() throws Exception
    {
        //register two remote objects
        remoteObjectController.flushInstances();
        RPCClass rpcClass = MyRPCTestObject.registerRpcClass();
        remoteObjectController.registerClass(rpcClass);
        remoteObjectController.registerClass(rpcClass);
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject~\", \"params\" : [], \"id\": \"abcd1\" }"), null).toJsonString());
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject~\", \"params\" : [], \"id\": \"abcd2\" }"), null).toJsonString());
        verify(callBackMock, times(2)).rpcControllerGeneratedMessage(anyString());

        String instanceIdentifier1 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        String instanceIdentifier2 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[1];

        // Set a test property on both remote objects
        RPCNotification instanceEventRequest = new RPCNotification(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:!\", \"params\" : [\"" + instanceIdentifier1 + "\", \"" + MyRPCTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"value1\"] }"));
        remoteObjectController.handle(instanceEventRequest.toJsonString());

        instanceEventRequest = new RPCNotification(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:!\", \"params\" : [\"" + instanceIdentifier2 + "\", \"" + MyRPCTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"value2\"] }"));
        remoteObjectController.handle(instanceEventRequest.toJsonString());

        //call method on remote obj1 with remote obj 2 as parameter
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:" + MyRPCTestObject.TEST_PASSBYREF_METHOD_MAPPING_NAME + "\", \"params\" : [\"" + instanceIdentifier1 + "\",\"" + instanceIdentifier2 + "\"], \"id\": \"abcd3\" }"), null).toJsonString());

        // Explanation: this times(3) is due to the weird behavior of Mockito on argument capturing in verify. Verify actually
        // catches all invocations of the mocked stub including the previous ones.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callBackMock, times(3)).rpcControllerGeneratedMessage(captor.capture());
        assertEquals("{\\\"id\\\":\\\"abcd3\\\",\\\"result\\\":\\\"value2\\\"}", captor.getValue());
    }

    //This test is non-deterministic for some reason. Although it works perfectly fine on local machine,
    // on Jenkins sometimes the instance identifier comes up wrong!
    public void IGNORE_testCallingInstancePropertiesWorks() throws Exception
    {
        //register two remote objects
        remoteObjectController.flushInstances();
        RPCClass rpcClass = MyRPCTestObject.registerRpcClass();
        remoteObjectController.registerClass(rpcClass);
        remoteObjectController.registerClass(rpcClass);
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject~\", \"params\" : [], \"id\": \"abcd1\" }"), null).toJsonString());
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject~\", \"params\" : [], \"id\": \"abcd2\" }"), null).toJsonString());
        verify(callBackMock, times(2)).rpcControllerGeneratedMessage(anyString());

        String instanceIdentifier1 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        String instanceIdentifier2 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[1];

        RPCNotification instanceEventRequest = new RPCNotification(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject:!\", \"params\" : [\"" + instanceIdentifier1 + "\", \"" + MyRPCTestObject.TEST_INSTANCE_PROPERTY_MAPPING_NAME + "\", \"" + instanceIdentifier2 + "\"] }"));
        remoteObjectController.handle(instanceEventRequest.toJsonString());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callBackMock, times(2)).rpcControllerGeneratedMessage(captor.capture());
        String arg = captor.getValue();
        JSONObject json = new JSONObject(convertStandardJSONString(arg));
        assertEquals(instanceIdentifier1, json.getString("result"));
    }

    //Utility Methods
    private void registerAndCreateTestObject() throws Exception
    {
        remoteObjectController.flushInstances();

        RPCClass rpcClass = MyRPCTestObject.registerRpcClass();
        remoteObjectController.registerClass(rpcClass);
        remoteObjectController.handle(new RPCRequest(new JSONObject("{ \"method\" : \"wisp.ai.MyRPCTestObject~\", \"params\" : [], \"id\": \"abcd4\" }"), null).toJsonString());
    }

    public String convertStandardJSONString(String data_json){
        data_json = data_json.replace("\\", "");
        data_json = data_json.replace("\"{", "{");
        data_json = data_json.replace("}\",", "},");
        data_json = data_json.replace("}\"", "}");
        return data_json;
    }
}


