package com.widespace.wisper.annotations;

import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.classrepresentation.WisperProperty;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This class automates Rpc class registration when a Java annotation is used.
 * All required to do is to have :
 * <p/>
 * <p/>
 * Created by Ehssan Hoorvash on 16/01/15.
 */
public class RPCClassRegistry
{

    public static WisperClassModel register(Class clazz) throws WisperException
    {
        Annotation annotation = clazz.getAnnotation(RPCClass.class);
        if (annotation != null)
            return registerWithAnnotations(clazz);
        else
            return registerWithoutAnnotations(clazz);
    }

    public static WisperClassModel registerWithoutAnnotations(Class clazz) throws WisperException
    {
        WisperClassModel wisperClassModel;
        try
        {
            Method registerRpcClassMethod = clazz.getDeclaredMethod("registerRpcClass");
            registerRpcClassMethod.setAccessible(true);
            wisperClassModel = (WisperClassModel) registerRpcClassMethod.invoke(null);
        } catch (NoSuchMethodException e)
        {
            throw new WisperException(Error.CLASS_NOT_WISPER_COMPATIBLE, e, "The class " + clazz.getName() + "is not Wisper compliant. Does it have the registerRpcClass() method?");
        } catch (InvocationTargetException e)
        {
            throw new WisperException(Error.CLASS_NOT_WISPER_COMPATIBLE, e, "The class " + clazz.getName() + "is not Wisper compliant. Does it have the registerRpcClass() method?");
        } catch (IllegalAccessException e)
        {
            throw new WisperException(Error.CLASS_NOT_WISPER_COMPATIBLE, e, "The class " + clazz.getName() + "is not Wisper compliant. Does it have the registerRpcClass() method?");
        }

        return wisperClassModel;
    }


    public static WisperClassModel registerWithAnnotations(Class clazz) throws WisperException
    {
        WisperClassModel wisperClassModel;

        wisperClassModel = fetchWisperClassModel(clazz);
        wisperClassModel = parseWisperProperties(wisperClassModel, clazz);
        wisperClassModel = parseWisperMethods(wisperClassModel, clazz);

        return wisperClassModel;
    }

    private static WisperClassModel fetchWisperClassModel(Class clazz) throws WisperException
    {
        Annotation annotation = clazz.getAnnotation(RPCClass.class);
        if (annotation == null)
            throw new WisperException(Error.CLASS_NOT_WISPER_COMPATIBLE, null, "The class " + clazz.getName() + "is not Wisper compliant. Does it have the annotation?");

        WisperClassModel wisperClassModel;//Class
        String mapName = ((RPCClass) annotation).name();
        wisperClassModel = new WisperClassModel(clazz, mapName);
        return wisperClassModel;
    }

    private static WisperClassModel parseWisperProperties(WisperClassModel wisperClassModel, Class clazz)
    {
        //Properties
        for (Field field : clazz.getDeclaredFields())
        {
            RPCProperty fieldAnnotation = field.getAnnotation(RPCProperty.class);

            String defaultSetterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            if (fieldAnnotation != null)
            {
                wisperClassModel.addProperty(new WisperProperty(fieldAnnotation.name(), fieldAnnotation.mode(), defaultSetterName, fieldAnnotation.paramType()));
            }
        }

        return wisperClassModel;
    }

    private static WisperClassModel parseWisperMethods(WisperClassModel wisperClassModel, Class clazz)
    {
        //Methods
        for (Method method : clazz.getDeclaredMethods())
        {
            for (Annotation methodAnnotation : method.getDeclaredAnnotations())
            {
                if (methodAnnotation instanceof RPCInstanceMethod)
                {
                    WisperParameterType[] associatedRpcParameters = getAssociatedMethodParameters(method);
                    WisperMethod instanceMethod = new WisperMethod(((RPCInstanceMethod) methodAnnotation).name(), method.getName(), associatedRpcParameters);
                    wisperClassModel.addInstanceMethod(instanceMethod);
                } else if (methodAnnotation instanceof RPCStaticMethod)
                {
                    WisperParameterType[] associatedRpcParameters = getAssociatedMethodParameters(method);
                    WisperMethod staticMethod = new WisperMethod(((RPCStaticMethod) methodAnnotation).name(), method.getName(), associatedRpcParameters);
                    wisperClassModel.addStaticMethod(staticMethod);
                }
            }
        }

        return wisperClassModel;
    }

    private static WisperParameterType[] getAssociatedMethodParameters(Method method) throws WisperException
    {
        Class[] methodParameterTypes = method.getParameterTypes();
        try
        {
            return RPCUtilities.convertParameterTypesToRPCParameterType(Arrays.asList(methodParameterTypes));

        } catch (IllegalArgumentException e)
        {
            throw new WisperException(Error.METHOD_INVALID_ARGUMENTS, null, "Method " + method.getName() + " parameters were not compatible with Wisper.");
        }

    }


}
