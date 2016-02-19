package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.controller.RemoteObjectController;


class RoutesTestObject implements Wisper
{

    private boolean destructCalled = false;

    public static WisperClassModel registerRpcClass()
    {
        //1.Build a class model
        WisperClassModel classModel = new WisperClassModel(RoutesTestObject.class, "wisp.router.test");

        //2.Build class methods of instance or static methods you might need
        WisperMethod appendMethod = new WisperMethod("append", "appendString", WisperParameterType.STRING, WisperParameterType.STRING);
        WisperMethod appendStaticMethod = new WisperMethod("append", "appendStringStatic", WisperParameterType.STRING, WisperParameterType.STRING);

        //3. Add the method models to your class model
        classModel.addInstanceMethod(appendMethod);
        classModel.addStaticMethod(appendStaticMethod);


        //4. Return the class model
        return classModel;
    }


    public static String appendStringStatic(String first, String second)
    {
        return first + second;
    }

    public String appendString(String first, String second)
    {
        return first + second;
    }

    @Override
    public void setRemoteObjectController(RemoteObjectController remoteObjectController)
    {

    }

    @Override
    public void destruct()
    {
        destructCalled = true;
    }

    public boolean destructCalled()
    {
        return destructCalled;
    }
}
