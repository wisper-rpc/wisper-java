package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.controller.RemoteObjectController;


class RoutesTestObject implements Wisper
{

    private static boolean staticMethodCalled = false;
    private static String printedValue = null;
    private String id = "default";
    private boolean destructCalled = false;
    private boolean instanceMethodCalled = false;

    public static WisperClassModel registerRpcClass()
    {
        //1.Build a class model
        WisperClassModel classModel = new WisperClassModel(RoutesTestObject.class, "wisp.router.test");

        //2.Build class methods of instance or static methods you might need
        WisperMethod appendMethod = new WisperMethod("append", "appendString", WisperParameterType.STRING, WisperParameterType.STRING);
        WisperMethod printMethod = new WisperMethod("printInstanceId", "printInstanceId", WisperParameterType.INSTANCE, WisperParameterType.STRING);


        WisperMethod appendStaticMethod = new WisperMethod("append", "appendStringStatic", WisperParameterType.STRING, WisperParameterType.STRING);
        WisperMethod printStaticMethod = new WisperMethod("printInstanceId", "printInstanceIdStatic", WisperParameterType.INSTANCE, WisperParameterType.STRING);

        //3. Add the method models to your class model
        classModel.addInstanceMethod(appendMethod);
        classModel.addInstanceMethod(printMethod);

        classModel.addStaticMethod(appendStaticMethod);
        classModel.addStaticMethod(printStaticMethod);


        //4. Return the class model
        return classModel;
    }


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
        printedValue = instance.getId() + message;
    }

    public void printInstanceId(RoutesTestObject instance, String message)
    {
        printedValue = instance.getId() + message;
    }


    //region
    @Override
    public void setRemoteObjectController(RemoteObjectController remoteObjectController)
    {

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
    }

    public static String printedValue()
    {
        return printedValue;
    }

    public void setTestId(String newId)
    {
        this.id = newId;
    }

    public String getId()
    {
        return this.id;
    }
}
