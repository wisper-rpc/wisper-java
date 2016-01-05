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
    private WisperClassModel wisperClassModel;
    private String SAMPLE_OBJECT;


    @Before
    public void setUp() throws Exception
    {
        SAMPLE_OBJECT = "my Test Object";
    }

    @Test
    public void testMapNameIsCorrect() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        assertThat(SAMPLE_MAP_NAME, is(equalTo(wisperClassModel.getMapName())));
    }

    @Test
    public void testMapNameCanBeChanged() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        wisperClassModel.setMapName("NEW");
        assertThat("NEW", is(equalTo(wisperClassModel.getMapName())));
    }

    @Test
    public void testClassRefIsCorrect() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        assertThat(SAMPLE_OBJECT, is(instanceOf(wisperClassModel.getClassRef())));
    }

    @Test
    public void testClassRefCouldBeOverwritten() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        wisperClassModel.setClassRef(this.getClass());
        assertThat(this, is(instanceOf(wisperClassModel.getClassRef())));
    }


    @Test
    public void testAddingStaticMethod() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        WisperMethod someMethod = new WisperMethod("methodMap", "someName");
        wisperClassModel.addStaticMethod(someMethod);
        assertThat(wisperClassModel.getStaticMethods().containsKey("methodMap"), is(true));
    }

    @Test
    public void testAddingInstanceMethod() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);
        WisperMethod someMethod = new WisperMethod("methodMap", "someName");
        wisperClassModel.addInstanceMethod(someMethod);
        assertThat(wisperClassModel.getInstanceMethods().containsKey("methodMap"), is(true));
    }

    @Test
    public void testPropertiesAreAddedCorrectly() throws Exception
    {
        wisperClassModel = new WisperClassModel(SAMPLE_OBJECT.getClass(), SAMPLE_MAP_NAME);

        WisperProperty property1 = new WisperProperty("prop1");
        WisperProperty property2 = new WisperProperty("prop2", WisperPropertyAccess.READ_WRITE, "setterName", WisperParameterType.STRING);

        wisperClassModel.addProperty(property1);
        wisperClassModel.addProperty(property2);

        assertThat(wisperClassModel.getProperties(), is(notNullValue()));
        assertThat(wisperClassModel.getProperties().size(), is(2));
        assertThat(wisperClassModel.getProperties().get(property1.getMappingName()), is(property1));
        assertThat(wisperClassModel.getProperties().get(property2.getMappingName()), is(property2));
    }

    @After
    public void tearDown() throws Exception
    {
        wisperClassModel = null;
    }
}
