package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.RPCEventBuilder;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
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
        classRouter = new ClassRouter(RoutesTestObject.class);
    }

    @Test
    public void givenClass_setsClassModel() throws Exception
    {
        assertThat(classRouter.getWisperClassModel(), is(notNullValue()));
        //assertThat(classRouter.getWisperClassModel(), is(equalTo(RoutesTestObject.registerRpcClass()))); needs equals() and hashCode() on classModel
    }

    @Test
    public void givenCreateMessage_InstanceIsSaved() throws Exception
    {
        ROUTE_PATH = "whatever";
        Request request = new Request(new JSONObject("{ \"method\" : \"" + ROUTE_PATH + "~\", \"params\" : [], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"));
        classRouter.routeMessage(request, "someclass~");
        assertThat(WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(classRouter), is(notNullValue()));
    }

    @Test
    public void givenDestructMessage_InstanceIsRemoved() throws Exception
    {
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
        ROUTE_PATH = "whatever.append";
        Request request = new Request().withMethodName("a.b.c.append").withParams(new Object[]{"x1", "x2"});

        assertThat(RoutesTestObject.staticMethodCalled(), is(false));
        classRouter.routeMessage(request, ROUTE_PATH);

        assertThat(RoutesTestObject.staticMethodCalled(), is(true));
    }

    @Test
    public void givenStaticMethodWithWisperInstanceParam_methodGetsCalled() throws Exception
    {
        ROUTE_PATH = "whatever.append";
        RoutesTestObject anInstance = new RoutesTestObject();
        String INSTANCE_TEST_ID = "Test-123";
        anInstance.setTestId(INSTANCE_TEST_ID);

        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), anInstance, "ABCD-1");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);
        Request request = new Request().withMethodName("a.b.c.printInstanceId").withParams(new Object[]{instanceModel1.getInstanceIdentifier(), "suffix"});

        classRouter.routeMessage(request, ROUTE_PATH);

        assertThat(anInstance.printedValue(), is(INSTANCE_TEST_ID + "suffix"));
    }

    @Test
    public void givenInstanceMethodWithWisperInstanceParam_methodGetsCalled() throws Exception
    {
        ROUTE_PATH = "whatever:append";
        String INSTANCE_TEST_ID = "Test-123";
        String PARAM_INSTACNE_TEST_ID = "ANOTHER";

        RoutesTestObject anInstance = new RoutesTestObject();
        RoutesTestObject anotherInstance = new RoutesTestObject();
        anInstance.setTestId(INSTANCE_TEST_ID);
        anotherInstance.setTestId(PARAM_INSTACNE_TEST_ID);

        WisperInstanceModel object_instance = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), anInstance, "ABCD-1");
        WisperInstanceModel param_instance = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), anotherInstance, "ABCD-2");
        WisperInstanceRegistry.sharedInstance().addInstance(object_instance, classRouter);
        WisperInstanceRegistry.sharedInstance().addInstance(param_instance, classRouter);
        Request request = new Request().withMethodName("a.b.c:printInstanceId").withParams(new Object[]{object_instance.getInstanceIdentifier(), param_instance.getInstanceIdentifier(), "suffix"});

        classRouter.routeMessage(request, ROUTE_PATH);

        assertThat(anInstance.printedValue(), is(PARAM_INSTACNE_TEST_ID + "suffix"));
    }

    @Test(expected = WisperException.class)
    public void givenUndefinedMethod_exceptionIsThrown() throws Exception
    {
        ROUTE_PATH = "whatever";
        RoutesTestObject anInstance = new RoutesTestObject();
        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), anInstance, "ABCD-1");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);
        Request request = new Request().withMethodName("a.b.c.undefinedMethodName").withParams(new Object[]{});

        classRouter.routeMessage(request, ROUTE_PATH);
    }

    @Test
    public void givenStaticEvent_eventHandlerIsCalledOnTestClass() throws Exception
    {
        ROUTE_PATH = "whatever";
        Event staticEventMessage = new RPCEventBuilder().withName("nonExistingPropertyName").withMethodName("something.somethingElse").buildStaticEvent();

        assertThat(RoutesTestObject.isStaticEventReceived(), is(false));
        classRouter.routeMessage(staticEventMessage, ROUTE_PATH);
        assertThat(RoutesTestObject.isStaticEventReceived(), is(true));
    }

    @Test
    public void givenInstanceEvent_eventHandlerIsCalledOnTestClass() throws Exception
    {
        ROUTE_PATH = "whatever";
        RoutesTestObject anInstance = new RoutesTestObject();
        WisperInstanceModel instanceModel1 = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), anInstance, "ABCD-1");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel1, classRouter);
        Event staticEventMessage = new RPCEventBuilder().withName("nonExistingPropertyName").withMethodName("something.somethingElse").withInstanceIdentifier(instanceModel1.getInstanceIdentifier()).buildInstanceEvent();

        assertThat(anInstance.isInstanceEventReceived(), is(false));
        classRouter.routeMessage(staticEventMessage, ROUTE_PATH);
        assertThat(anInstance.isInstanceEventReceived(), is(true));
    }
}




