package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
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
}