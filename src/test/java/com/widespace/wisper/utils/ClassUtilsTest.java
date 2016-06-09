package com.widespace.wisper.utils;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.route.ClassRouter;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


public class ClassUtilsTest
{
    @Test
    public void isPrimitive_worksOnPrimitiveTypes() throws Exception
    {
        assertThat(ClassUtils.isPrimitive(int.class), is(true));
        assertThat(ClassUtils.isPrimitive(double.class), is(true));
        assertThat(ClassUtils.isPrimitive(float.class), is(true));
        assertThat(ClassUtils.isPrimitive(short.class), is(true));
        assertThat(ClassUtils.isPrimitive(long.class), is(true));
        assertThat(ClassUtils.isPrimitive(char.class), is(true));
        assertThat(ClassUtils.isPrimitive(boolean.class), is(true));
    }

    @Test
    public void testFetchProperties() throws Exception
    {
        WisperClassModel classModel = UtilitiesTestObject.registerClassModel();
        WisperInstanceModel instanceModel = new WisperInstanceModel(classModel, new UtilitiesTestObject(), "id-1234");
        HashMap<String, Object> properties = ClassUtils.fetchInitializedProperties(instanceModel, classModel);

        assertThat(properties, is(notNullValue()));
        assertThat(properties.size(), is(1));
        assertThat(properties.containsKey("prop1"), is(true));
        assertThat(properties.containsKey("prop2"), is(false));
    }
}





//region Test Object

class UtilitiesTestObject implements Wisper
{

    String prop1;
    String prop2;

    public UtilitiesTestObject()
    {
        this.prop1 = "init1";
        this.prop2 = "init2";
    }

    public static WisperClassModel registerClassModel()
    {
        WisperClassModel model = new WisperClassModel(UtilitiesTestObject.class);

        model.addProperty(new WisperProperty("prop1", WisperPropertyAccess.READ_WRITE, "setProp1", WisperParameterType.STRING));

        return model;
    }

    public String getProp1()
    {
        return prop1;
    }

    public void setProp1(String prop1)
    {
        this.prop1 = prop1;
    }

    public String getProp2()
    {
        return prop2;
    }

    public void setProp2(String prop2)
    {
        this.prop2 = prop2;
    }

    @Override
    public void setClassRouter(ClassRouter classRouter)
    {

    }

    @Override
    public void destruct()
    {

    }
}