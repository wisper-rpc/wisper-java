package com.widespace.wisper.base;

import com.widespace.wisper.classrepresentation.RPCMethodParameterType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ehssan Hoorvash on 23/10/14.
 */
public class RPCUtilities
{
    public static Class[] convertRpcParameterTypeToClassType(List<RPCMethodParameterType> paramTypes)
    {
        ArrayList<Class> result = new ArrayList<Class>();

        for (RPCMethodParameterType paramType : paramTypes)
        {
            switch (paramType)
            {

                case STRING:
                    result.add(String.class);
                    break;
                case NUMBER:
                    result.add(Number.class);
                    break;
                case ARRAY:
                    result.add(List.class);
                    break;
                case HASHMAP:
                    result.add(HashMap.class);
                    break;
                case BOOLEAN:
                    result.add(Boolean.class);
                    break;
                case INSTANCE:   // leave it as it is!
                    result.add(RPCMethodParameterType.INSTANCE.getClass());
                default:
                    break;
            }
        }

        return result.toArray(new Class[result.size()]);
    }

    public static RPCMethodParameterType[] convertParameterTypesToRPCParameterType(List<Class> paramTypes) throws IllegalArgumentException
    {
        ArrayList<RPCMethodParameterType> result = new ArrayList<RPCMethodParameterType>();
        for (Class paramType : paramTypes)
        {
            if (paramType.equals(String.class))
            {
                result.add(RPCMethodParameterType.STRING);
            }
            else if (paramType.equals(Number.class))
            {
                result.add(RPCMethodParameterType.NUMBER);
            }
            else if (paramType.equals(HashMap.class))
            {
                result.add(RPCMethodParameterType.HASHMAP);
            }
            else if (paramType.equals(Boolean.class))
            {
                result.add(RPCMethodParameterType.BOOLEAN);
            }
            else
            {
                throw new IllegalArgumentException("RPC Methods can only have Boolean, Hashmap, String and Number as their parameter types");
            }
        }

        return result.toArray(new RPCMethodParameterType[result.size()]);
    }

    public static Class[] convertRpcParameterTypeToClassType(RPCMethodParameterType paramTypes)
    {
        ArrayList<RPCMethodParameterType> types = new ArrayList<RPCMethodParameterType>();
        types.add(paramTypes);
        return convertRpcParameterTypeToClassType(types);
    }

    public static Object[] addAll(Object[] array1, Object[] array2)
    {
        if (array1 == null)
        {
            return array2;
        }
        else if (array2 == null)
        {
            return array1;
        }
        Object[] joinedArray = (Object[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try
        {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        }
        catch (ArrayStoreException ase)
        {
            // Check if problem was due to incompatible types
              /*
               * We do this here, rather than before the copy because:
               * - it would be a wasted check most of the time
               * - safer, in case check turns out to be too strict
               */
            final Class type1 = array1.getClass().getComponentType();
            final Class type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2))
            {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName());
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }
}
