package com.widespace.wisper;

import com.widespace.wisper.annotations.RPCAnnotationTest;
import com.widespace.wisper.annotations.RPCClassRegistry;
import com.widespace.wisper.classrepresentation.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;


public class WisperClassModelRegistryTest
{

    private WisperClassModel registeredClass;

    @Before
    public void setUp() throws Exception
    {
        registeredClass = RPCClassRegistry.register(RPCAnnotationTest.class);
    }

    @Test
    public void testShouldNotReturnNull() throws Exception
    {
        assertNotNull(registeredClass);
    }

    @Test
    public void testShouldRegistersClassWithCorrectMapName() throws Exception
    {
        assertEquals("wisp.test.annotationTest", registeredClass.getMapName());
    }

    @Test
    public void testShouldRegisterClassWithCorrectProperties() throws Exception
    {
        HashMap<String, WisperProperty> properties = registeredClass.getProperties();

        assertEquals(1, properties.size());
        assertEquals(WisperPropertyAccess.READ_WRITE, properties.get("value").getMode());
        assertEquals("setTestProperty", properties.get("value").getSetterName());
        assertEquals(WisperParameterType.NUMBER, properties.get("value").getSetterMethodParameterType());
    }


    @Test
    public void testShouldRegisterClassWithCorrectInstanceMethods() throws Exception
    {
        HashMap<String, WisperMethod> instanceMethods = registeredClass.getInstanceMethods();

        assertEquals(1, instanceMethods.size());
        assertEquals("testAddInstance", instanceMethods.get("add").getMethodName());
    }

    @Test
    public void testShouldRegisterClassWithCorrectStaticMethods() throws Exception
    {
        HashMap<String, WisperMethod> staticMethods = registeredClass.getStaticMethods();

        assertEquals(1, staticMethods.size());
        assertEquals("testAddStatic", staticMethods.get("add").getMethodName());
    }

    @After
    public void tearDown() throws Exception
    {
        registeredClass = null;
    }
}

