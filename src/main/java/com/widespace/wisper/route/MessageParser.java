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
    /**
     * Returns the call type of the message based on method name.
     *
     * @param methodName String containing method name of the message.
     * @return Call type as a WisperCallType
     * @see WisperCallType
     */
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

    /**
     * Returns the call type of the message based on method name.
     *
     * @param message Wisper message.
     * @return Call type as a WisperCallType
     * @see WisperCallType
     */
    public static WisperCallType getCallType(AbstractMessage message)
    {
        String methodName = getFullMethodName(message);
        return getCallType(methodName);
    }

    /**
     * Returns the method name based on what message type it is. For instance, "a.b.c:m" will result in "m" as the method type.
     *
     * @param message message to be parsed
     * @return a string representing parsed method name, or null if nothing matches or message type is unknown.
     */
    public static String getMethodName(AbstractMessage message)
    {
        String result = null;
        String fullMethodName = getFullMethodName(message);
        if (fullMethodName != null)
        {
            ArrayList<String> classComponents = new ArrayList<String>(Arrays.asList(fullMethodName.split("\\.")));
            String lastComponent = classComponents.get(classComponents.size() - 1);
            switch (getCallType(message))
            {
                case CREATE_INSTANCE:
                    result = Constants.CONSTRUCTOR_TOKEN;
                    break;
                case DESTROY_INSTANCE:
                    result = ":" + Constants.CONSTRUCTOR_TOKEN;
                    break;
                case STATIC_METHOD:
                    result = lastComponent;
                    break;
                case INSTANCE_METHOD:
                {
                    ArrayList<String> components = new ArrayList<String>(Arrays.asList(lastComponent.split(":")));
                    result = components.get(components.size() - 1);
                }
                break;
                case STATIC_EVENT:
                case INSTANCE_EVENT:
                case UNKNOWN:
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    /**
     * Returns the full method name that is embodied with the message. In case message is something other than Notification
     * or Request, the result is null.
     *
     * @param message message to be parsed.
     * @return a String with method name, or null.
     */
    public static String getFullMethodName(AbstractMessage message)
    {
        if (message instanceof Notification)
            return ((Notification) message).getMethodName();

        if (message instanceof Request)
            return ((Request) message).getMethodName();

        return null;
    }


    /**
     * Returns true if the message has parameters, false otherwise.
     *
     * @param message a Wisper message
     * @return true if the message has parameters, false otherwise.
     */
    public static boolean hasParams(AbstractMessage message)
    {
        return getParams(message) != null && getParams(message).length > 0;
    }

    /**
     * Returns the parameters of the message.
     *
     * @param message the Wisper message
     * @return parameters gotten from the message.
     */
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

    /**
     * Returns the class name from the message.
     *
     * @param message Wisper message
     * @return A string containing the class name from the message.
     */
    public static String getClassName(AbstractMessage message)
    {
        String result = null;
        String className = getFullMethodName(message);
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

    /**
     * Returns insteance identifier of the message, if anyt
     *
     * @param message
     * @return
     */
    public static String getInstanceIdentifier(AbstractMessage message)
    {
        switch (getCallType(message))
        {
            case DESTROY_INSTANCE:
            case INSTANCE_METHOD:
            case INSTANCE_EVENT:
            {
                if (message instanceof Notification)
                {
                    Object[] params = ((Notification) message).getParams();
                    if (params == null || params.length == 0)
                    {
                        return null;
                    } else
                    {
                        return (String) params[0];
                    }
                } else if (message instanceof Request)
                {
                    Object[] params = ((Request) message).getParams();
                    if (params == null || params.length == 0)
                    {
                        return null;
                    } else
                    {
                        //TODO: Handle "params":[null] case. could cause a crash here.
                        return (String) params[0];
                    }
                }
            }
            case UNKNOWN:
            case CREATE_INSTANCE:
            case STATIC_METHOD:
            case STATIC_EVENT:
                return null;
        }

        return null;
    }
}
