package com.widespace.wisper.route;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ClassRouterTest
{
    private ClassRouter classRouter;

    @Before
    public void setUp() throws Exception
    {

    }

    @Test
    public void givenClass_setsClassModel() throws Exception
    {
        classRouter = new ClassRouter(RoutesTestObject.class);
        assertThat(classRouter.getWisperClassModel(), is(notNullValue()));
        //assertThat(classRouter.getWisperClassModel(), is(equalTo(RoutesTestObject.registerRpcClass()))); needs equals() and hashCode() on classModel
    }



}




