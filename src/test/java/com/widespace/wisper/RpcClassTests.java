package com.widespace.wisper;

import com.widespace.wisper.classrepresentation.*;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ehssan Hoorvash on 10/06/14.
 */

public class RpcClassTests
{
    private RPCClass rpcClass;
    private String myTestObject;


    @Before
    public void setUp() throws Exception
    {
        myTestObject = "my Test Object";
        rpcClass = new RPCClass(myTestObject.getClass(), "theMapName");
    }

    @Test
    public void testMapNameIsCorrect() throws Exception
    {
        assertEquals("RPC map name is not correct", rpcClass.getMapName(), "theMapName");
    }

    @Test
    public void testClassRefIsCorrect() throws Exception
    {
        assertEquals("RPC map name is not correct", rpcClass.getClassRef(), myTestObject.getClass());
    }

    @Test
    public void testAddingStaticMethod() throws Exception
    {
        RPCClassMethod someMethod = new RPCClassMethod("methodMap", "someName");
        rpcClass.addStaticMethod(someMethod);
        assertTrue(rpcClass.getStaticMethods().containsKey("methodMap"));
    }

    @Test
    public void testAddingInstanceMethod() throws Exception
    {
        RPCClassMethod someMethod = new RPCClassMethod("methodMap", "someName");
        rpcClass.addInstanceMethod(someMethod);
        assertTrue(rpcClass.getInstanceMethods().containsKey("methodMap"));
    }

    @Test
    public void testPropertiesAreAddedCorrectly() throws Exception
    {
        RPCClassProperty property1 = new RPCClassProperty("prop1");
        RPCClassProperty property2 = new RPCClassProperty("prop2", RPCClassPropertyMode.READ_WRITE, "setterName", RPCMethodParameterType.STRING);

        rpcClass.addProperty(property1);
        rpcClass.addProperty(property2);

        assertNotNull(rpcClass.getProperties());
        assertEquals(2, rpcClass.getProperties().size());
        assertEquals(property1, rpcClass.getProperties().get(property1.getMappingName()));
        assertEquals(property2, rpcClass.getProperties().get(property2.getMappingName()));
    }
}
