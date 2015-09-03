package com.widespace.wisper;

import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.RPCRequest;
import com.widespace.wisper.messagetype.RPCResponse;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Created by Ehssan Hoorvash on 13/06/14.
 */
public class RpcRequestTests
{
    private static final String SAMPLE_REQUEST = "{\"method\":\"wisp.ai.TestObject:sampleMethodName\", \"params\":[\"sample_instance_identifier\"],\"id\":\"abcd1\"}";
    private RPCRequest request;

    @Before
    public void setUp() throws Exception
    {

        ResponseBlock responseBlockMock = mock(ResponseBlock.class);

        request = new RPCRequest(new JSONObject(SAMPLE_REQUEST),  responseBlockMock);
    }

    @Test
    public void testToStringWorksFine() throws Exception
    {
        JSONAssert.assertEquals(new JSONObject(SAMPLE_REQUEST), request.toJson(), false);
    }

    @Test
    public void testMethodName() throws Exception
    {
        assertEquals("wisp.ai.TestObject:sampleMethodName", request.getMethodName());
    }

    @Test
    public void testIdentifier() throws Exception
    {
        assertEquals("abcd1", request.getIdentifier());
    }

    @Test
    public void testResponseCreationWorks() throws Exception
    {
        RPCResponse expectedResponse = new RPCResponse(new JSONObject("{\"result\":[],\"id\":\"abcd1\"}"), request);
        JSONAssert.assertEquals(expectedResponse.toJson(), request.createResponse().toJson(), false);

    }

    @Test
    public void testSettingParameters() throws Exception
    {
        Object[] params = request.getParams();
        assertEquals(1, params.length);
        assertEquals("sample_instance_identifier", (String) params[0]);
    }

    @Test
    public void testDeterminingParameterTypes() throws Exception
    {
        String requestStr = "{\"id\":\"abcd1\" , \"method\":\"wisp.ai.TestObject.sampleMethodName\", \"params\":" +
                "[" +
                "\"some_string\", " +                                       //a string
                "225.008, " +                                                //a number
                "[\"string_in_array_idx1\", \"string_in_array_idx2\"]," +    //an array
                "{\"key1\":\"value1\",\"key2\":\"value2\"}" +                // a json obj (hashmap)
                " ]" +
                "}";

        request = new RPCRequest(new JSONObject(requestStr),  mock(ResponseBlock.class));
        Object[] requestParams = request.getParams();

        //Number of params
        assertEquals(4, requestParams.length);

        //String
        assertEquals("some_string", requestParams[0]);

        //Number
        assertEquals(225.008, requestParams[1]);

        //Array
        Object[] innerArray = {"string_in_array_idx1", "string_in_array_idx2"};
        List<Object> expected = Arrays.asList(innerArray);
        assertEquals(expected, requestParams[2]);

        //Hashmap (json obj)
        HashMap<String, String> innerObject = new HashMap<String, String>();
        innerObject.put("key1", "value1");
        innerObject.put("key2", "value2");
        assertEquals(innerObject, requestParams[3]);
    }

    @Test
    public void testNestedArrayInHashmapParamDetermination() throws Exception
    {
        String requestStr = "{\"id\":\"abcd1\" , \"method\":\"wisp.ai.TestObject.sampleMethodName\", \"params\":" +
                "[" +
                "[\"string_in_array_idx1\", 225.008 , {\"key1\":\"value1\",\"key2\":\"value2\"}]" +    //an array containing a number, a string, and an object
                "]" +
                "}";

        request = new RPCRequest(new JSONObject(requestStr),  mock(ResponseBlock.class));
        Object[] requestParams = request.getParams();

        assertEquals(1, requestParams.length);

        ArrayList<Object> expected = new ArrayList<Object>();
        expected.add("string_in_array_idx1");
        expected.add(225.008);
        HashMap<String, String> innerObject = new HashMap<String, String>();
        innerObject.put("key1", "value1");
        innerObject.put("key2", "value2");
        expected.add(innerObject);

        assertEquals(expected, requestParams[0]);
    }


    @Test
    public void testNestedHashmapInArrayParamDetermination() throws Exception
    {
        String requestStr = "{\"id\":\"abcd1\" , \"method\":\"wisp.ai.TestObject.sampleMethodName\", \"params\":" +
                "[" +
                "{\"first\" : [1,2,3],\"second\":\"string_in_obj\",\"third\":225.008,\"fourth\":{\"inner_first\":[1,2,3,\"oh shit it's actually working!\"]}}" +    //an object containing an array and another obj that contains an array, a string and a number
                "]" +
                "}";

        request = new RPCRequest(new JSONObject(requestStr),  mock(ResponseBlock.class));
        Object[] requestParams = request.getParams();

        assertEquals(1, requestParams.length);

        HashMap<String, Object> expected = new HashMap<String, Object>();
        expected.put("first", Arrays.asList(1, 2, 3));
        expected.put("second", "string_in_obj");
        expected.put("third", 225.008);
        HashMap<String, Object> innerJsonObj = new HashMap<String, Object>();
        innerJsonObj.put("inner_first", Arrays.asList(1, 2, 3, "oh shit it's actually working!"));
        expected.put("fourth", innerJsonObj);

        assertEquals(expected, requestParams[0]);

    }
}
