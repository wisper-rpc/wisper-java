package com.widespace.wisper.route;


import com.widespace.wisper.base.Constants;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageParser
{


    public static WisperCallType getCallType(String methodName)
    {
        if (methodName != null)
        {
            ArrayList<String> components = new ArrayList<String>(Arrays.asList(methodName.split(":")));
            String lastComponent = components.get(components.size() - 1);

            if (components.size() > 1)
            {
                //Cases like ObjectName:~
                if (lastComponent.contains(Constants.CONSTRUCTOR_TOKEN))
                {
                    return WisperCallType.DESTROY_INSTANCE;
                }
                //Cases like ObjectName:!
                else if (lastComponent.contains("!"))
                {
                    return WisperCallType.INSTANCE_EVENT;
                }
                //Cases like X:x
                else
                {
                    return WisperCallType.INSTANCE_METHOD;
                }
            }

            //Cases like X~
            if (lastComponent.contains(Constants.CONSTRUCTOR_TOKEN))
            {
                return WisperCallType.CREATE_INSTANCE;
            }

            //Cases like X!
            if (lastComponent.contains("!"))
            {
                return WisperCallType.STATIC_EVENT;
            }

            //Cases like X.x
            if (lastComponent.contains("."))
            {
                return WisperCallType.STATIC_METHOD;
            }
        }

        return WisperCallType.UNKNOWN;
    }

    public static WisperCallType getCallType(AbstractMessage message)
    {
        String methodName = getMethodName(message);
        return getCallType(methodName);
    }

    private static String getMethodName(AbstractMessage message)
    {
        if (message instanceof Notification)
            return ((Notification) message).getMethodName();

        if (message instanceof Request)
            return ((Request) message).getMethodName();

        return null;
    }


    public static boolean hasParams(AbstractMessage message)
    {
        return getParams(message) != null && getParams(message).length > 0;
    }

    public static Object[] getParams(AbstractMessage message)
    {
        Object[] parameters = null;
        int index = 0;

        switch (getCallType(message))
        {
            case DESTROY_INSTANCE:
            case INSTANCE_METHOD:
            case INSTANCE_EVENT:
                index = 1;
                break;
            case STATIC_EVENT:
                index = 0;
                break;
            default:
                break;
        }

        if (message instanceof Request)
        {
            parameters = ((Request) message).getParams();
        }

        if (message instanceof Notification)
        {
            parameters = ((Notification) message).getParams();
        }

        //Cut off the first parameter if index = 1, otherwise returns teh whole list.
        if (parameters != null)
        {
            List<Object> paramList = Arrays.asList(parameters);
            List<Object> objectList = paramList.subList(index, paramList.size());
            parameters = objectList.toArray();
        }

        return parameters;
    }

    public String getClassName(AbstractMessage message)
    {
        String result = null;
        String className = getMethodName(message);
        if (className != null)
        {
            switch (getCallType(message))
            {
                case UNKNOWN:
                    break;
                case CREATE_INSTANCE:
                    result = className.substring(0, className.length() - 1);
                    break;
                case DESTROY_INSTANCE:
                {
                    result = className.split(":")[0];
                }
                break;
                case STATIC_METHOD:

                    String[] splitted = StringUtils.split(className, ".");
                    String lastComp = splitted[splitted.length - 1];
                    result = className.substring(0, className.lastIndexOf(lastComp) - 1);

                    break;
                case STATIC_EVENT:
                    result = className.split("!")[0];
                    break;

                case INSTANCE_METHOD:
                {
                    result = className.split(":")[0];
                }
                break;
                case INSTANCE_EVENT:
                    break;
            }
        }

        return result;
    }
}
