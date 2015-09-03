package com.widespace.wisper.utils;

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

    public static boolean isPrimitive(Class clazz)
    {
        return (clazz.isPrimitive() || objectTobuiltInMap.containsKey(clazz) && objectTobuiltInMap.get(clazz).isPrimitive());

    }

}
