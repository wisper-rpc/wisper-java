package com.widespace.wisper.annotations;

import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.RPCClassMethod;
import com.widespace.wisper.classrepresentation.RPCClassProperty;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *  This class automates Rpc class registration when a Java annotation is used.
 *  All required to do is to have :
 *
 *
 * Created by Ehssan Hoorvash on 16/01/15.
 */
public class RPCClassRegistry
{
    public static WisperClassModel register(Class clazz)
    {
        WisperClassModel wisperClassModel = null;

        for (Annotation annotation : clazz.getDeclaredAnnotations())
        {
            if (annotation instanceof com.widespace.wisper.annotations.RPCClass)
            {
                //Class
                String mapName = ((com.widespace.wisper.annotations.RPCClass) annotation).name();
                wisperClassModel = new WisperClassModel(clazz, mapName);

                //Properties
                for (Field field : clazz.getDeclaredFields())
                {
                    RPCProperty fieldAnnotation = field.getAnnotation(RPCProperty.class);

                    String defaultSetterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                    if (fieldAnnotation != null)
                    {
                        wisperClassModel.addProperty(new RPCClassProperty(fieldAnnotation.name(), fieldAnnotation.mode(), defaultSetterName, fieldAnnotation.paramType()));
                    }
                }

                //Methods
                for (Method method : clazz.getDeclaredMethods())
                {
                    for (Annotation methodAnnotation : method.getDeclaredAnnotations())
                    {
                        if (methodAnnotation instanceof RPCInstanceMethod)
                        {
                            RPCMethodParameterType[] associatedRpcParameters = getAssociatedRpcParameters(method);
                            RPCClassMethod instanceMethod = new RPCClassMethod(((RPCInstanceMethod) methodAnnotation).name(), method.getName(), associatedRpcParameters);
                            wisperClassModel.addInstanceMethod(instanceMethod);
                        }
                        else if (methodAnnotation instanceof RPCStaticMethod)
                        {
                            RPCMethodParameterType[] associatedRpcParameters = getAssociatedRpcParameters(method);
                            RPCClassMethod staticMethod = new RPCClassMethod(((RPCStaticMethod) methodAnnotation).name(), method.getName(), associatedRpcParameters);
                            wisperClassModel.addStaticMethod(staticMethod);
                        }
                    }
                }
            }
        }

        return wisperClassModel;
    }

    private static RPCMethodParameterType[] getAssociatedRpcParameters(Method method)
    {
        Class[] methodParameterTypes = method.getParameterTypes();
        try
        {
            return RPCUtilities.convertParameterTypesToRPCParameterType(Arrays.asList(methodParameterTypes));

        }
        catch (IllegalArgumentException e)
        {
            // An illegal method parameter type was passed in.
            e.printStackTrace();
            return null;
        }

    }


}
