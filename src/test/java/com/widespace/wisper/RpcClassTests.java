package com.widespace.wisper;

import com.widespace.wisper.classrepresentation.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class RpcClassTests
{
    private final String SAMPLE_MAP_NAME = "SAMPLE_MAP_NAME";
    private RPCClass rpcClass;
    private String SAMPLE_OBJECT;


    @Before
    public void setUp() throws Exception
    {
        SAMPLE_OBJECT = "my Test Object";
    }

    @Test
    public void testMapNameIsCorrect() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        assertThat(SAMPLE_MAP_NAME, is(equalTo(rpcClass.getMapName())));
    }

    @Test
    public void testMapNameCanBeChanged() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        rpcClass.setMapName("NEW");
        assertThat("NEW", is(equalTo(rpcClass.getMapName())));
    }

    @Test
    public void testClassRefIsCorrect() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        assertThat(SAMPLE_OBJECT, is(instanceOf(rpcClass.getClassRef())));
    }

    @Test
    public void testClassRefCouldBeOverwritten() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        rpcClass.setClassRef(this.getClass());
        assertThat(this, is(instanceOf(rpcClass.getClassRef())));
    }


    @Test
    public void testAddingStaticMethod() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        RPCClassMethod someMethod = new RPCClassMethod("methodMap", "someName");
        rpcClass.addStaticMethod(someMethod);
        assertThat(rpcClass.getStaticMethods().containsKey("methodMap"), is(true));
    }

    @Test
    public void testAddingInstanceMethod() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        RPCClassMethod someMethod = new RPCClassMethod("methodMap", "someName");
        rpcClass.addInstanceMethod(someMethod);
        assertThat(rpcClass.getInstanceMethods().containsKey("methodMap"), is(true));
    }

    @Test
    public void testPropertiesAreAddedCorrectly() throws Exception
    {
        rpcClass = new RPCClass(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);

        RPCClassProperty property1 = new RPCClassProperty("prop1");
        RPCClassProperty property2 = new RPCClassProperty("prop2", RPCClassPropertyMode.READ_WRITE, "setterName", RPCMethodParameterType.STRING);

        rpcClass.addProperty(property1);
        rpcClass.addProperty(property2);

        assertThat(rpcClass.getProperties(), is(notNullValue()));
        assertThat(rpcClass.getProperties().size(), is(2));
        assertThat(rpcClass.getProperties().get(property1.getMappingName()), is(property1));
        assertThat(rpcClass.getProperties().get(property2.getMappingName()), is(property2));
    }

    @After
    public void tearDown() throws Exception
    {
        rpcClass = null;
    }
}
