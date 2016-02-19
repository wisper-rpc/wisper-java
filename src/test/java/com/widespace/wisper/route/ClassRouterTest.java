package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.Request;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ClassRouterTest
{
    private static final String SAMPLE_REQUEST_ID = "abcd1";
    private ClassRouter classRouter;
    private String ROUTE_PATH;

    @Before
    public void setUp() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
    }

    @Test
    public void givenClass_setsClassModel() throws Exception
    {
        classRouter = new ClassRouter(RoutesTestObject.class);
        assertThat(classRouter.getWisperClassModel(), is(notNullValue()));
        //assertThat(classRouter.getWisperClassModel(), is(equalTo(RoutesTestObject.registerRpcClass()))); needs equals() and hashCode() on classModel
    }

    @Test
    public void givenCreateMessage_InstanceIsSaved() throws Exception
    {
        sendCreateRequestToClassRouter();
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(notNullValue()));
    }

    @Test
    public void givenDestructMessage_InstanceIsRemoved() throws Exception
    {
        //create the class first
        sendCreateRequestToClassRouter();
        ROUTE_PATH = "wisp.router.someclass";
        Request destruct = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + ":~\", \"params\" : [], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter.routeMessage(destruct, "whatever:~");
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(nullValue()));

    }

    private void sendCreateRequestToClassRouter()
    {
        Request request = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + "~\", \"params\" : [], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter = new ClassRouter(RoutesTestObject.class);
        classRouter.routeMessage(request, "someclass~");
    }
}




