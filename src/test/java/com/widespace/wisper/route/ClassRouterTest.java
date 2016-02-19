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
        Request request = new Request(new JSONObject("{ \"method\" : \"wisp.router.someclass~\", \"params\" : [], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter = new ClassRouter(RoutesTestObject.class);
        WisperInstanceRegistry.sharedInstance().clear();
        classRouter.routeMessage(request, "someclass~");
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(notNullValue()));
    }
}




