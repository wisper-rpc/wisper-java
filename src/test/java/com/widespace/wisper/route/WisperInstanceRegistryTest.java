package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WisperInstanceRegistryTest
{

    private WisperInstanceRegistry instanceRegistry;

    @Before
    public void setUp() throws Exception
    {
        instanceRegistry = WisperInstanceRegistry.sharedInstance();
    }

    @After
    public void tearDown() throws Exception
    {
        instanceRegistry.clear();
    }

    @Test
    public void addsInstancesUnderRoute() throws Exception
    {
        Router router = mock(Router.class);
        WisperInstanceModel instanceModel = mock(WisperInstanceModel.class);
        when(instanceModel.getInstanceIdentifier()).thenReturn("ABC");

        instanceRegistry.addInstance(instanceModel, router);
        assertThat(instanceRegistry.getInstances(), is(notNullValue()));
        assertThat(instanceRegistry.getInstancesUnderRoute(router), is(notNullValue()));
        assertThat(instanceRegistry.getInstancesUnderRoute(router).get("ABC"), is(instanceModel));
    }

    @Test
    public void givenExistingId_canFindIdUnderRoute() throws Exception
    {
        Router router = mock(Router.class);
        String instanceIdentifier = "mock-id";
        WisperInstanceModel instanceModel = new WisperInstanceModel(mock(WisperClassModel.class), mock(Wisper.class), instanceIdentifier);
        instanceRegistry.addInstance(instanceModel, router);

        WisperInstanceModel found = instanceRegistry.findInstanceUnderRoute(instanceIdentifier, router);
        assertThat(found, is(instanceModel));
    }


    @Test
    public void givenInstanceId_canFindInstance() throws Exception
    {
        Router router = mock(Router.class);
        String instanceIdentifier = "mock-id";
        WisperInstanceModel instanceModel = new WisperInstanceModel(mock(WisperClassModel.class), mock(Wisper.class), instanceIdentifier);
        instanceRegistry.addInstance(instanceModel, router);

        WisperInstanceModel found = instanceRegistry.findInstanceWithId(instanceIdentifier);
        assertThat(found, is(instanceModel));
    }

    @Test
    public void givenInstanceId_canFindRouter() throws Exception
    {
        Router router = mock(Router.class);
        String instanceIdentifier = "mock-id";
        WisperInstanceModel instanceModel = new WisperInstanceModel(mock(WisperClassModel.class), mock(Wisper.class), instanceIdentifier);
        instanceRegistry.addInstance(instanceModel, router);

        Router found = instanceRegistry.findRouterForInstanceId(instanceIdentifier);
        assertThat(found, is(router));
    }

    @Test
    public void givenExistingInstance_findsTheWisperInstanceIfRegistered() throws Exception
    {
        Router router = mock(Router.class);
        RoutesTestObject actualInstance = new RoutesTestObject();
        WisperInstanceModel instanceModel = new WisperInstanceModel(RoutesTestObject.registerRpcClass(), actualInstance, "ABCD-1");
        instanceRegistry.addInstance(instanceModel, router.getRootRoute());

        WisperInstanceModel instanceModelUnderRoute = instanceRegistry.findInstanceUnderRoute(actualInstance, router.getRootRoute());

        assertThat(instanceModelUnderRoute, is(notNullValue()));
        assertThat(instanceModelUnderRoute.getInstanceIdentifier(), is("ABCD-1"));
        assertThat(instanceModelUnderRoute, is(instanceModel));
    }
}