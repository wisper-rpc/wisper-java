package com.widespace.wisper.base;


import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.classrepresentation.RPCClassMethod;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;

/**
 * Merely for testing purposes
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public class WisperTestObject extends WisperObject
{
    public static String appendStringStatic(String first, String second)
    {
        return first + second;
    }

    /**
     * This method must be implemented by any object desiring to register itself as RPC
     * unfortunately prior to Java 8 we cannot have static methods in Interfaces which is why we need to take this approach instead.
     *
     * @return RPCClass an instance of the RPC class model object containing the models of methods
     */
    public static RPCClass registerRpcClass()
    {
        //1.Build a class model
        RPCClass classModel = new RPCClass(WisperTestObject.class, "wisp.test.TestObject");

        //2.Build class methods of instance or static methods you might need
        RPCClassMethod appendMethod = new RPCClassMethod("append", "appendString", RPCMethodParameterType.STRING, RPCMethodParameterType.STRING);
        RPCClassMethod appendStaticMethod = new RPCClassMethod("append", "appendStringStatic", RPCMethodParameterType.STRING, RPCMethodParameterType.STRING);
        RPCClassMethod exceptionInMethodCallMethod = new RPCClassMethod("exceptionInMethodCall", "exceptionInMethodCall");
        RPCClassMethod exceptionInMethodCallStaticMethod = new RPCClassMethod("exceptionInMethodCall", "exceptionInMethodCallStatic");

        //3. Add the method models to your class model
        classModel.addInstanceMethod(appendMethod);
        classModel.addStaticMethod(appendStaticMethod);

        classModel.addInstanceMethod(exceptionInMethodCallMethod);
        classModel.addStaticMethod(exceptionInMethodCallStaticMethod);

        //4. Return the class model
        return classModel;
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

}
