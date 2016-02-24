package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.Request;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
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
        RoutesTestObject.reset();
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
        classRouter = new ClassRouter(RoutesTestObject.class);
        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), new RoutesTestObject(), "ABCD-1");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);

        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).size(), is(1));
        ROUTE_PATH = "ABCD-1";
        Request destruct = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + ":~\", \"params\" : [" + instanceModel1.getInstanceIdentifier() + "], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter.routeMessage(destruct, "whatever:~");

        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(notNullValue()));
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).size(), is(0));
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).get(instanceModel1.getInstanceIdentifier()), is(nullValue()));
    }

    @Test
    public void givenDestructMessage_OnlyThatSpecificInstanceIsRemoved() throws Exception
    {
        classRouter = new ClassRouter(RoutesTestObject.class);
        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), new RoutesTestObject(), "ABCD-1");
        WisperInstanceModel instanceModel2 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), new RoutesTestObject(), "ABCD-2");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel2, classRouter);

        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).size(), is(2));
        ROUTE_PATH = "ABCD-1";
        Request destruct = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + ":~\", \"params\" : [" + instanceModel1.getInstanceIdentifier() + "], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter.routeMessage(destruct, "whatever:~");

        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(notNullValue()));
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).size(), is(1));
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter).get(instanceModel2.getInstanceIdentifier()), is(instanceModel2));
    }

    @Test
    public void givenInstanceMethodCall_methodWillGetCalledOnActualInstance() throws Exception
    {
        classRouter = new ClassRouter(RoutesTestObject.class);
        RoutesTestObject actualInstance = new RoutesTestObject();
        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), actualInstance, "ABCD-1");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);

        ROUTE_PATH = "whatever:append";
        Request request = new Request().withMethodName("a.b.c:append").withParams(new Object[]{instanceModel1.getInstanceIdentifier(), "x1", "x2"});

        assertThat(actualInstance.instanceMethodCalled(), is(false));
        classRouter.routeMessage(request, ROUTE_PATH);

        assertThat(actualInstance.instanceMethodCalled(), is(true));
    }

    @Test
    public void givenStaticMethodCall_methodWillGetCalledOnActualClass() throws Exception
    {
        classRouter = new ClassRouter(RoutesTestObject.class);

        ROUTE_PATH = "whatever.append";
        Request request = new Request().withMethodName("a.b.c.append").withParams(new Object[]{"x1", "x2"});

        assertThat(RoutesTestObject.staticMethodCalled(), is(false));
        classRouter.routeMessage(request, ROUTE_PATH);

        assertThat(RoutesTestObject.staticMethodCalled(), is(true));
    }

    private void sendCreateRequestToClassRouter()
    {
        Request request = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + "~\", \"params\" : [], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter = new ClassRouter(RoutesTestObject.class);
        classRouter.routeMessage(request, "someclass~");
    }
}




