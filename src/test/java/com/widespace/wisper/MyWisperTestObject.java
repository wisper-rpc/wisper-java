package com.widespace.wisper;

import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.route.ClassRouter;

// This class is needed for RPC tests.
public class MyWisperTestObject extends WisperObject
{
    public static final String TEST_INSTANCE_METHOD_MAPPING_NAME = "testMethod1";
    public static final String TEST_STATIC_METHOD_MAPPING_NAME = "testStaticMethod1";
    public static final String TEST_UNREGISTERED_INSTANCE_METHOD_MAPPING_NAME = "unregisteredInstanceMethod";
    public static final String TEST_UNREGISTERED_STATIC_METHOD_MAPPING_NAME = "unregisteredStaticMethod";

    public static final String TEST_PROPERTY_MAPPING_NAME = "prop";
    public static final String TEST_PASSBYREF_METHOD_MAPPING_NAME = "passByRef";
    public static final String TEST_INSTANCE_PROPERTY_MAPPING_NAME = "instanceProp";
    public static final String TEST_STATIC_PROPERTY_MAPPING_NAME = "staticProp";

    public static String propertyValue = null;

    public Wisper instanceProperty = null;

    private static String lasteMethodCalled = null;

    private String property = null;
    public static String staticProp = null;


    public static String testStaticMethod1(String message)
    {
        lasteMethodCalled = TEST_STATIC_METHOD_MAPPING_NAME;
        return "I just echo " + message;
    }

    public static WisperClassModel registerRpcClass()
    {
        lasteMethodCalled = null;

        //1.Build a class model
        WisperClassModel classModel = new WisperClassModel(MyWisperTestObject.class, "wisp.ai.MyWisperTestObject");

        //2.Build class methods of instance or static methods you might need
        WisperMethod testMethod1 = new WisperMethod(TEST_INSTANCE_METHOD_MAPPING_NAME, "testMethod1", WisperParameterType.STRING);
        WisperMethod testStaticMethod1 = new WisperMethod(TEST_STATIC_METHOD_MAPPING_NAME, "testStaticMethod1", WisperParameterType.STRING);

        WisperMethod testPassByRef = new WisperMethod(TEST_PASSBYREF_METHOD_MAPPING_NAME, "printObject", WisperParameterType.INSTANCE);

        //3.Add Properties if any
        WisperProperty property = new WisperProperty("prop", WisperPropertyAccess.READ_WRITE, "setProperty", WisperParameterType.STRING);
        WisperProperty propertyInstance = new WisperProperty(TEST_INSTANCE_PROPERTY_MAPPING_NAME, WisperPropertyAccess.READ_WRITE, "setInstanceProperty", WisperParameterType.INSTANCE);

        //3. Add the method models to your class model
        classModel.addInstanceMethod(testMethod1);
        classModel.addStaticMethod(testStaticMethod1);
        classModel.addInstanceMethod(testPassByRef);

        classModel.addProperty(property);
        classModel.addProperty(propertyInstance);

        //4. Return the class model
        return classModel;
    }


    public static String getLastMethodCalled()
    {
        return lasteMethodCalled;
    }

    public String testMethod1(String message)
    {
        lasteMethodCalled = TEST_INSTANCE_METHOD_MAPPING_NAME;
        return "I just echo " + message;
    }

    public String unregisteredInstanceMethod()
    {
        lasteMethodCalled = TEST_UNREGISTERED_INSTANCE_METHOD_MAPPING_NAME;
        return "unregistered";
    }

    public String unregisteredStaticMethod()
    {
        lasteMethodCalled = TEST_UNREGISTERED_STATIC_METHOD_MAPPING_NAME;
        return "unregistered";
    }

    public void setProperty(String property)
    {
        this.property = property;
        propertyValue = property;
    }

    public String getProperty()
    {
        return property;
    }

    public void setInstanceProperty(MyWisperTestObject instanceProperty)
    {
        this.instanceProperty = instanceProperty;
    }

    public String printObject(MyWisperTestObject obj)
    {
        return obj.property;
    }


    @Override
    public void setClassRouter(ClassRouter classRouter)
    {

    }
}
