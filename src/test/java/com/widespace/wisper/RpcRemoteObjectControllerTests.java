package com.widespace.wisper;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.controller.RemoteObjectController;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
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
    private RemoteObjectController remoteObjectController;
    private GatewayCallback callBackMock;


    @Before
    public void setUp() throws Exception
    {
        callBackMock = mock(GatewayCallback.class);
        remoteObjectController = new RemoteObjectController(callBackMock);
    }

    @Test
    public void testClassConversionForNonRegisteredClassReturnsNull() throws Exception
    {
        WisperClassModel wisperClassForClassModel = remoteObjectController.getRpcClassForClass(String.class);
        assertNull(wisperClassForClassModel);
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
        WisperClassModel wisperClassModel = MyWisperTestObject.registerRpcClass();
        remoteObjectController.registerClass(wisperClassModel);
        assertEquals(remoteObjectController.getRpcClassForClass(MyWisperTestObject.class), wisperClassModel);
    }

    @Test
    public void testCanHandleRequestForInstanceCreation() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());
    }

    @Test
    public void testCanHandleRequestForInstanceDestruction() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //destruct
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        Request sampleRequest = new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:~\", \"params\" : [\"" + instanceIdentifier + "\"], \"id\": \"abcd5\" }"), null);
        remoteObjectController.handleMessage(sampleRequest.toJsonString());

        assertTrue(remoteObjectController.getInstanceMap().isEmpty());
    }

    @Test
    public void testCallingInstanceMethodsWorks() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //Call instance method
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        Request instanceMethodRequest = new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:" + MyWisperTestObject.TEST_INSTANCE_METHOD_MAPPING_NAME + "\", \"params\" : [\"" + instanceIdentifier + "\", \"something_else\"], \"id\": \"abcd4\" }"), null);
        remoteObjectController.handleMessage(instanceMethodRequest.toJsonString());

        assertEquals(MyWisperTestObject.TEST_INSTANCE_METHOD_MAPPING_NAME, MyWisperTestObject.getLastMethodCalled());
    }

    @Test
    public void testCallingStaticMethodWorks() throws Exception
    {
        registerAndCreateTestObject();
        assertFalse(remoteObjectController.getInstanceMap().isEmpty());

        //Call instance method
        Request staticMethodRequest = new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject." + MyWisperTestObject.TEST_STATIC_METHOD_MAPPING_NAME + "\", \"params\" : [\"some_string_param\"], \"id\": \"abcd4\" }"), null);
        remoteObjectController.handleMessage(staticMethodRequest.toJsonString());

        assertEquals(MyWisperTestObject.TEST_STATIC_METHOD_MAPPING_NAME, MyWisperTestObject.getLastMethodCalled());
    }

    @Test
    public void testRPCPropertiesAreSetWithInstanceEvents() throws Exception
    {
        registerAndCreateTestObject();
        String instanceIdentifier = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        Request instanceEventRequest = new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:!\", \"params\" : [\"" + instanceIdentifier + "\", \"" + MyWisperTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"new_prop_value\"] }"), null);
        remoteObjectController.handleMessage(instanceEventRequest.toJsonString());

        assertEquals("new_prop_value", MyWisperTestObject.propertyValue);
    }



    @Ignore
    @Test
    //Test might be actually wrong.
    public void testCallingPassByReferenceWorksOnInstanceMethods() throws Exception
    {
        //register two remote objects
        remoteObjectController.flushInstances();
        WisperClassModel wisperClassModel = MyWisperTestObject.registerRpcClass();
        remoteObjectController.registerClass(wisperClassModel);
        remoteObjectController.registerClass(wisperClassModel);
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject~\", \"params\" : [], \"id\": \"abcd1\" }"), null).toJsonString());
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject~\", \"params\" : [], \"id\": \"abcd2\" }"), null).toJsonString());
        verify(callBackMock, times(2)).gatewayGeneratedMessage(anyString());

        String instanceIdentifier1 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        String instanceIdentifier2 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[1];

        // Set a test property on both remote objects
        Notification instanceEventRequest = new Notification(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:!\", \"params\" : [\"" + instanceIdentifier1 + "\", \"" + MyWisperTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"value1\"] }"));
        remoteObjectController.handleMessage(instanceEventRequest.toJsonString());

        instanceEventRequest = new Notification(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:!\", \"params\" : [\"" + instanceIdentifier2 + "\", \"" + MyWisperTestObject.TEST_PROPERTY_MAPPING_NAME + "\", \"value2\"] }"));
        remoteObjectController.handleMessage(instanceEventRequest.toJsonString());

        //call method on remote obj1 with remote obj 2 as parameter
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:" + MyWisperTestObject.TEST_PASSBYREF_METHOD_MAPPING_NAME + "\", \"params\" : [\"" + instanceIdentifier1 + "\",\"" + instanceIdentifier2 + "\"], \"id\": \"abcd3\" }"), null).toJsonString());

        // Explanation: this times(3) is due to the weird behavior of Mockito on argument capturing in verify. Verify actually
        // catches all invocations of the mocked stub including the previous ones.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callBackMock, times(3)).gatewayGeneratedMessage(captor.capture());
        assertEquals("{\\\"id\\\":\\\"abcd3\\\",\\\"result\\\":\\\"value2\\\"}", captor.getValue());
    }

    //This test is non-deterministic for some reason. Although it works perfectly fine on local machine,
    // on Jenkins sometimes the instance identifier comes up wrong!
    @Ignore
    @Test
    public void testCallingInstancePropertiesWorks() throws Exception
    {
        //register two remote objects
        remoteObjectController.flushInstances();
        WisperClassModel wisperClassModel = MyWisperTestObject.registerRpcClass();
        remoteObjectController.registerClass(wisperClassModel);
        remoteObjectController.registerClass(wisperClassModel);
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject~\", \"params\" : [], \"id\": \"abcd1\" }"), null).toJsonString());
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject~\", \"params\" : [], \"id\": \"abcd2\" }"), null).toJsonString());
        verify(callBackMock, times(2)).gatewayGeneratedMessage(anyString());

        String instanceIdentifier1 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[0];
        String instanceIdentifier2 = (String) remoteObjectController.getInstanceMap().keySet().toArray()[1];

        Notification instanceEventRequest = new Notification(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject:!\", \"params\" : [\"" + instanceIdentifier1 + "\", \"" + MyWisperTestObject.TEST_INSTANCE_PROPERTY_MAPPING_NAME + "\", \"" + instanceIdentifier2 + "\"] }"));
        remoteObjectController.handleMessage(instanceEventRequest.toJsonString());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callBackMock, times(2)).gatewayGeneratedMessage(captor.capture());
        String arg = captor.getValue();
        JSONObject json = new JSONObject(convertStandardJSONString(arg));

        assertEquals(instanceIdentifier1, json.getString("result"));
    }

    //Utility Methods

    private void registerAndCreateTestObject() throws Exception
    {
        remoteObjectController.flushInstances();

        WisperClassModel wisperClassModel = MyWisperTestObject.registerRpcClass();
        remoteObjectController.registerClass(wisperClassModel);
        remoteObjectController.handleMessage(new Request(new JSONObject("{ \"method\" : \"wisp.ai.MyWisperTestObject~\", \"params\" : [], \"id\": \"abcd4\" }"), null).toJsonString());
    }


    public String convertStandardJSONString(String data_json){
        data_json = data_json.replace("\\", "");
        data_json = data_json.replace("\"{", "{");
        data_json = data_json.replace("}\",", "},");
        data_json = data_json.replace("}\"", "}");
        return data_json;
    }
}


