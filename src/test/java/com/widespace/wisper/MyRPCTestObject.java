package com.widespace.wisper;

import com.widespace.wisper.base.RPCObject;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;

// This class is needed for RPC tests.
public class MyRPCTestObject extends RPCObject
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

    public static RPCClass registerRpcClass()
    {
        lasteMethodCalled = null;

        //1.Build a class model
        RPCClass classModel = new RPCClass(MyRPCTestObject.class, "wisp.ai.MyRPCTestObject");

        //2.Build class methods of instance or static methods you might need
        RPCClassMethod testMethod1 = new RPCClassMethod(TEST_INSTANCE_METHOD_MAPPING_NAME, "testMethod1", RPCMethodParameterType.STRING);
        RPCClassMethod testStaticMethod1 = new RPCClassMethod(TEST_STATIC_METHOD_MAPPING_NAME, "testStaticMethod1", RPCMethodParameterType.STRING);

        RPCClassMethod testPassByRef = new RPCClassMethod(TEST_PASSBYREF_METHOD_MAPPING_NAME, "printObject", RPCMethodParameterType.INSTANCE);

        //3.Add Properties if any
        RPCClassProperty property = new RPCClassProperty("prop", RPCClassPropertyMode.READ_WRITE, "setProperty", RPCMethodParameterType.STRING);
        RPCClassProperty propertyInstance = new RPCClassProperty(TEST_INSTANCE_PROPERTY_MAPPING_NAME, RPCClassPropertyMode.READ_WRITE, "setInstanceProperty", RPCMethodParameterType.INSTANCE);

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
        propertyValue = this.property;
    }

    public void setInstanceProperty(Wisper instanceProperty)
    {
        this.instanceProperty = instanceProperty;
    }

    public String printObject(MyRPCTestObject obj)
    {
        return obj.property;
    }


}
