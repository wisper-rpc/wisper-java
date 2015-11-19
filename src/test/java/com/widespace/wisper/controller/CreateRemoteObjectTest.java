package com.widespace.wisper.controller;


import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.MessageFactory;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CreateRemoteObjectTest
{
    RemoteObjectController remoteObjectController;
    private Request creationRequest;
    private GatewayCallback callbackMock;
    private final String SAMPLE_REQUEST_ID = "abcd4";

    @Before
    public void setUp() throws Exception
    {
        callbackMock = mock(GatewayCallback.class);
        remoteObjectController = new RemoteObjectController(callbackMock);
        remoteObjectController.registerClass(WisperControllerTestObject.registerRpcClass());
        creationRequest = new Request(new JSONObject("{ \"method\" : \"wisp.test.ControllerTest~\", \"params\" : [\"testString\"], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"), null);

        //expected response something similar to //{"result":{"id":"com.widespace.wisper.controller.WisperControllerTestObject@4b4523f8","props":{"property":"initialized"}},"id":"abcd4"}
    }

    @After
    public void tearDown() throws Exception
    {
        remoteObjectController.getInstanceMap().clear();
        remoteObjectController = null;
        callbackMock = null;
    }

    @Test
    public void testCreateObjectReturnsNonNull() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        assertThat(remoteObjectController.getInstanceMap().size(), is(1));
    }

    @Test
    public void testCreateObjectFiresResponse() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        verify(callbackMock).gatewayGeneratedMessage(anyString());
    }

    @Test
    public void testCreateObjectFiresResponseWithValidJsonString() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());

        AbstractMessage message = new MessageFactory().createMessage(new JSONObject(captor.getValue()));
        assertThat(message, CoreMatchers.is(CoreMatchers.<AbstractMessage>instanceOf(Response.class)));

    }

    @Test
    public void testCreateObjectPassesBackChangedParameters() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());

        JSONObject response = new JSONObject(captor.getValue());
        JSONObject result = response.getJSONObject("result");

        assertThat(result.has("props"), is(true));
        assertThat(result.get("props"), CoreMatchers.<Object>is(instanceOf(JSONObject.class)));

        JSONObject props = result.getJSONObject("props");
        assertThat(props.has("property"), is(true));
        assertThat(props.getString("property"), is(equalTo("initialized")));

    }

    @Test
    public void testCreateObjectRespondsWithTheSameIdAsRequest() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());
        JSONObject response = new JSONObject(captor.getValue());

        assertThat(response.getString("id"), is(equalTo(SAMPLE_REQUEST_ID)));
    }

    @Test
    public void testCreateCanTakeParameters() throws Exception
    {
        remoteObjectController.handleMessage(creationRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());
        System.out.println(captor.getValue());
    }

    //Override constructor tests
    @Test
    public void testCreateCanBeOverriddenWithInstanceBlocks() throws Exception
    {
        remoteObjectController.registerClass(OverriddenConstructorTestObject.registerRpcClassWithInstanceBlock());
        creationRequest = new Request(new JSONObject("{ \"method\" : \"wisp.test.OverrideConstructorTest~\", \"params\" : [\"testString\"], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"), null);
        remoteObjectController.handleMessage(creationRequest);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());

        JSONObject response = new JSONObject(captor.getValue());
        JSONObject result = response.getJSONObject("result");

        assertThat(result.has("props"), is(true));
        assertThat(result.get("props"), CoreMatchers.<Object>is(instanceOf(JSONObject.class)));

        JSONObject props = result.getJSONObject("props");
        assertThat(props.has("property"), is(true));
        assertThat(props.getString("property"), is(equalTo("initialized")));

    }

    @Test
    public void testCreateCanBeOverriddenWithStaticBlocks() throws Exception
    {
        remoteObjectController = new RemoteObjectController(callbackMock);
        remoteObjectController.registerClass(OverriddenConstructorTestObject.registerRpcClassWithStaticBlock());
        creationRequest = new Request(new JSONObject("{ \"method\" : \"wisp.test.OverrideConstructorTest~\", \"params\" : [\"testString\"], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"), null);
        remoteObjectController.handleMessage(creationRequest);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(callbackMock).gatewayGeneratedMessage(captor.capture());

        JSONObject response = new JSONObject(captor.getValue());
        JSONObject result = response.getJSONObject("result");

        assertThat(result.has("props"), is(true));
        assertThat(result.get("props"), CoreMatchers.<Object>is(instanceOf(JSONObject.class)));

        JSONObject props = result.getJSONObject("props");
        assertThat(props.has("property"), is(true));
        assertThat(props.getString("property"), is(equalTo("initialized")));

    }
}