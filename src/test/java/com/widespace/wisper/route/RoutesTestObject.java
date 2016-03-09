package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.Event;


class RoutesTestObject implements Wisper
{

    private static boolean staticMethodCalled = false;
    private static String printedValue = null;
    private static boolean staticEventReceived = false;
    private String testId = "default";
    private boolean destructCalled = false;
    private boolean instanceMethodCalled = false;

    public static String testProp;
    private String prop;
    private boolean instanceEventReceived;

    public static WisperClassModel registerRpcClass()
    {
        //1.Build a class model
        WisperClassModel classModel = new WisperClassModel(RoutesTestObject.class, "wisp.router.test");

        //2.Build class methods of instance or static methods you might need
        WisperMethod appendMethod = new WisperMethod("append", "appendString", WisperParameterType.STRING, WisperParameterType.STRING);
        WisperMethod printMethod = new WisperMethod("printInstanceId", "printInstanceId", WisperParameterType.INSTANCE, WisperParameterType.STRING);

        WisperMethod customConstructor = new WisperMethod("~", "RoutesTestObject", WisperParameterType.STRING);
        WisperMethod appendStaticMethod = new WisperMethod("append", "appendStringStatic", WisperParameterType.STRING, WisperParameterType.STRING);
        WisperMethod printStaticMethod = new WisperMethod("printInstanceId", "printInstanceIdStatic", WisperParameterType.INSTANCE, WisperParameterType.STRING);

        WisperProperty staticProperty = new WisperProperty("testProp", WisperPropertyAccess.READ_WRITE, "setTestProp", WisperParameterType.STRING);
        WisperProperty instanceProperty = new WisperProperty("prop", WisperPropertyAccess.READ_WRITE, "setProp", WisperParameterType.STRING);

        //3. Add the method models to your class model
        classModel.addInstanceMethod(appendMethod);
        classModel.addInstanceMethod(printMethod);

        classModel.addStaticMethod(customConstructor);
        classModel.addStaticMethod(appendStaticMethod);
        classModel.addStaticMethod(printStaticMethod);

        classModel.addProperty(staticProperty);
        classModel.addProperty(instanceProperty);

        //4. Return the class model
        return classModel;
    }



    //region Constructors
    //==========================================================================
    public RoutesTestObject()
    {

    }

    public RoutesTestObject(String id)
    {
        this.testId = id;
        this.prop = "set-by-constructor";
    }


    //region Wisper Event Handlers
    //===========================================================================
    public static void wisperStaticEventHandler(Event event)
    {
        if (event != null)
            staticEventReceived = true;
    }

    public void wisperEventHandler(Event event)
    {
        if (event != null)
            instanceEventReceived = true;
    }


    //region Getter Setter
    //===========================================================================


    public static String getTestProp()
    {
        return testProp;
    }

    public static void setTestProp(String testProp)
    {
        RoutesTestObject.testProp = testProp;
    }

    public String getProp()
    {
        return prop;
    }

    public void setProp(String prop)
    {
        this.prop = prop;
    }

    //region Methods
    //===========================================================================
    public static String appendStringStatic(String first, String second)
    {
        staticMethodCalled = true;
        return first + second;
    }


    public String appendString(String first, String second)
    {
        instanceMethodCalled = true;
        return first + second;
    }

    public static void printInstanceIdStatic(RoutesTestObject instance, String message)
    {
        printedValue = instance.getTestId() + message;
    }

    public void printInstanceId(RoutesTestObject instance, String message)
    {
        printedValue = instance.getTestId() + message;
    }


    @Override
    public void destruct()
    {
        destructCalled = true;
    }

    //region Utility Methods for Tests
    //==========================================================
    public boolean destructCalled()
    {
        return destructCalled;
    }

    public boolean instanceMethodCalled()
    {
        return instanceMethodCalled;
    }

    public static boolean staticMethodCalled()
    {
        return staticMethodCalled;
    }

    public static void reset()
    {
        staticMethodCalled = false;
        staticEventReceived = false;
    }

    public static String printedValue()
    {
        return printedValue;
    }

    public void setTestId(String newId)
    {
        this.testId = newId;
    }

    public String getTestId()
    {
        return this.testId;
    }

    public static boolean isStaticEventReceived()
    {
        return staticEventReceived;
    }

    public boolean isInstanceEventReceived()
    {
        return instanceEventReceived;
    }
}
