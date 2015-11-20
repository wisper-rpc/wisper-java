package com.widespace.wisper.controller;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.base.Constants;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by ehssanhoorvash on 19/11/15.
 */
public class OverriddenConstructorTestObject implements Wisper
{
    private String property;


    //================================================================================
    //region Constructors
    //================================================================================


    public OverriddenConstructorTestObject()
    {
        this.property = "initialized";
    }

    public OverriddenConstructorTestObject(String property)
    {
        this.property = property;
    }


    //================================================================================
    //region Getters and Setters
    //================================================================================

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }


    //================================================================================
    //region Wisper methods
    //================================================================================

    @Override
    public void setRemoteObjectController(RemoteObjectController remoteObjectController)
    {

    }

    @Override
    public void destruct()
    {

    }

    public static RPCClass registerRpcClassWithInstanceBlock()
    {
        final RPCClass classModel = new RPCClass(WisperControllerTestObject.class, "wisp.test.OverrideConstructorTest");

        classModel.addProperty(new RPCClassProperty("property", RPCClassPropertyMode.READ_WRITE, "setProperty", RPCMethodParameterType.STRING));

        classModel.addInstanceMethod(new RPCClassMethod(Constants.CONSTRUCTOR_TOKEN, new CallBlock()
        {
            @Override
            public void perform(RemoteObjectController remoteObjectController, WisperClassInstance classInstance, RPCClassMethod method, Request request)
            {
                WisperControllerTestObject instance = new WisperControllerTestObject();
                WisperClassInstance wisperClassInstance = remoteObjectController.addRpcObjectInstance(instance, classModel);

                HashMap<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put(Constants.ID, wisperClassInstance.getInstanceIdentifier());
                HashMap<String, Object> properties = new HashMap<String, Object>();
                try
                {
                    Field declaredField = instance.getClass().getDeclaredField("property");
                    declaredField.setAccessible(true);
                    properties.put("property", declaredField.get(instance));

                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally
                {
                    resultMap.put(Constants.PROPERTIES, properties);
                }

                Response response = request.createResponse();
                response.setResult(resultMap);
                request.getResponseBlock().perform(response, null);
            }
        }));

        return classModel;
    }

    public static RPCClass registerRpcClassWithStaticBlock()
    {
        final RPCClass classModel = new RPCClass(WisperControllerTestObject.class, "wisp.test.OverrideConstructorTest");

        classModel.addProperty(new RPCClassProperty("property", RPCClassPropertyMode.READ_WRITE, "setProperty", RPCMethodParameterType.STRING));

        classModel.addStaticMethod(new RPCClassMethod(Constants.CONSTRUCTOR_TOKEN, new CallBlock()
        {
            @Override
            public void perform(RemoteObjectController remoteObjectController, WisperClassInstance classInstance, RPCClassMethod method, Request request)
            {
                WisperControllerTestObject instance = new WisperControllerTestObject();
                WisperClassInstance wisperClassInstance = remoteObjectController.addRpcObjectInstance(instance, classModel);

                HashMap<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put(Constants.ID, wisperClassInstance.getInstanceIdentifier());
                HashMap<String, Object> properties = new HashMap<String, Object>();
                try
                {
                    Field declaredField = instance.getClass().getDeclaredField("property");
                    declaredField.setAccessible(true);
                    properties.put("property", declaredField.get(instance));

                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally
                {
                    resultMap.put(Constants.PROPERTIES, properties);
                }

                Response response = request.createResponse();
                response.setResult(resultMap);
                request.getResponseBlock().perform(response, null);
            }
        }));

        return classModel;
    }
}
