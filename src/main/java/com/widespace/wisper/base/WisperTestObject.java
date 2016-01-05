package com.widespace.wisper.base;


import com.widespace.wisper.classrepresentation.*;

/**
 * Merely for testing purposes
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public class WisperTestObject extends WisperObject
{

    private String sampleProperty;

    public WisperTestObject(String sampleProperty)
    {
        this.sampleProperty = sampleProperty;
    }

    /**
     * This method must be implemented by any object desiring to register itself as RPC
     * unfortunately prior to Java 8 we cannot have static methods in Interfaces which is why we need to take this approach instead.
     *
     * @return WisperClassModel an instance of the RPC class model object containing the models of methods
     */
    public static WisperClassModel registerRpcClass()
    {
        //1.Build a class model
        WisperClassModel classModel = new WisperClassModel(WisperTestObject.class, "wisp.test.TestObject");

        //2.Build class methods of instance or static methods you might need
        RPCClassMethod appendMethod = new RPCClassMethod("append", "appendString", RPCMethodParameterType.STRING, RPCMethodParameterType.STRING);
        RPCClassMethod appendStaticMethod = new RPCClassMethod("append", "appendStringStatic", RPCMethodParameterType.STRING, RPCMethodParameterType.STRING);
        RPCClassMethod exceptionInMethodCallMethod = new RPCClassMethod("exceptionInMethodCall", "exceptionInMethodCall");
        RPCClassMethod exceptionInMethodCallStaticMethod = new RPCClassMethod("exceptionInMethodCall", "exceptionInMethodCallStatic");

        RPCClassProperty sampleProp = new RPCClassProperty("testProperty", RPCClassPropertyMode.READ_WRITE, "setSampleProperty", RPCMethodParameterType.STRING);

        //3. Add the method models to your class model
        classModel.addInstanceMethod(appendMethod);
        classModel.addStaticMethod(appendStaticMethod);

        classModel.addInstanceMethod(exceptionInMethodCallMethod);
        classModel.addStaticMethod(exceptionInMethodCallStaticMethod);
        classModel.addProperty(sampleProp);

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

    public void printString(String message)
    {
        System.out.println(message);
    }


    public void exceptionInMethodCall() throws Exception
    {
        throw new Exception("Test Exception");
    }

    public static void exceptionInMethodCallStatic() throws Exception
    {
        throw new Exception("Test Exception");
    }


    public String getSampleProperty()
    {
        return sampleProperty;
    }

    public void setSampleProperty(String sampleProperty)
    {
        this.sampleProperty = sampleProperty;
    }
}

