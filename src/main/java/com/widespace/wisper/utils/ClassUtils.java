package com.widespace.wisper.utils;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.classrepresentation.WisperProperty;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.route.WisperInstanceRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils
{
    private static Map<Class<?>, Class<?>> objectTobuiltInMap;

    static
    {
        objectTobuiltInMap = new HashMap<Class<?>, Class<?>>();
        objectTobuiltInMap.put(Boolean.class, boolean.class);
        objectTobuiltInMap.put(Character.class, char.class);
        objectTobuiltInMap.put(Byte.class, byte.class);
        objectTobuiltInMap.put(Short.class, short.class);
        objectTobuiltInMap.put(Integer.class, int.class);
        objectTobuiltInMap.put(Long.class, long.class);
        objectTobuiltInMap.put(Float.class, float.class);
        objectTobuiltInMap.put(Double.class, double.class);
        objectTobuiltInMap.put(Void.class, void.class);
    }

    public static boolean isPrimitive(@NotNull Class clazz)
    {
        return (clazz.isPrimitive() || objectTobuiltInMap.containsKey(clazz) && objectTobuiltInMap.get(clazz).isPrimitive());
    }


    public static Class<?>[] getParameterClasses(Object[] parameters)
    {
        Class<?>[] classes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++)
        {
            classes[i] = parameters[i].getClass();
        }

        return classes;
    }

    /**
     * Returns a Map of properties of a given instance where properties are both Wisper Properties and are also initialized during
     * object construction.
     *
     * @param wisperInstanceModel wisper instance whose properties to be looked up
     * @param classModel          class model of the same instance.
     * @return a map of initialized properties.
     * @throws WisperException An exception might be thrown in cases where the properties are poorly defined (no getter or setter for the properties), or where the
     *                         property is defined but not found.
     */
    public static HashMap<String, Object> fetchInitializedProperties(WisperInstanceModel wisperInstanceModel, WisperClassModel classModel) throws WisperException
    {
        HashMap<String, Object> initializedProperties = new HashMap<String, Object>();
        HashMap<String, WisperProperty> properties = classModel.getProperties();
        String currentPropertyName = null;
        try
        {
            for (String propertyName : properties.keySet())
            {
                currentPropertyName = propertyName;

                WisperProperty property = properties.get(propertyName);
                Wisper instance = wisperInstanceModel.getInstance();

                //Check if there is a getter implemented.
                Method getter = instance.getClass().getMethod(property.getSetterName().replace("set", "get"));
                getter.setAccessible(true);
                Object value = getter.invoke(instance);

                //Check if the value is something other than null (default)
                if (value == null)
                {
                    continue;
                }

                if (property.getSetterMethodParameterType() == WisperParameterType.INSTANCE)
                {
                    WisperInstanceModel WisperInstanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId((String) value);
                    value = WisperInstanceModel.getInstanceIdentifier();
                }

                initializedProperties.put(propertyName, value);
            }
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + " not found in class " + wisperInstanceModel.getWisperClassModel().getMapName() + ". Does the getter method actually exist in the class? ";
            throw new WisperException(Error.GETTER_METHOD_NOT_FOUND, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + " is not accessible in class " + wisperInstanceModel.getWisperClassModel().getMapName() + ". Is the getter method public?";
            throw new WisperException(Error.GETTER_METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + "could not be invoked in class  " + wisperInstanceModel.getWisperClassModel().getMapName();
            throw new WisperException(Error.GETTER_METHOD_INVOCATION_ERROR, e, errorMessage);
        }

        return initializedProperties;
    }

}
