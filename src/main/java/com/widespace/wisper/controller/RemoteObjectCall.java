package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.route.WisperCallType;
import com.widespace.wisper.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.widespace.wisper.route.WisperCallType.*;

/**
 * Created by Ehssan Hoorvash on 23/05/14.
 */
public class RemoteObjectCall
{
    public static final String CONSTRUCT_METHOD_NAME = "~";
    public static final String DESTRUCT_METHOD_NAME = "~";

    private String instanceIdentifier;

    private Request request;

    private Notification notification;

    public RemoteObjectCall(AbstractMessage message)
    {
        if (message instanceof Request)
        {
            this.request = (Request) message;
        }
        else if (message instanceof Notification)
        {
            this.notification = (Notification) message;
        }

        determineInstanceIdentifier();
    }

    private void determineInstanceIdentifier()
    {
        switch (getCallType())
        {

            case UNKNOWN:
            case CREATE_INSTANCE:
            case STATIC_METHOD:
            case STATIC_EVENT:
                instanceIdentifier = null;
                return;
            case DESTROY_INSTANCE:
            case INSTANCE_METHOD:
            case INSTANCE_EVENT:
            {
                if (notification != null)
                {
                    if (notification.getParams() == null || notification.getParams().length == 0)
                    {
                        instanceIdentifier = null;
                    }
                    else
                    {
                        instanceIdentifier = (String) notification.getParams()[0];
                    }
                }
                else if (request != null)
                {
                    if (request.getParams() == null || request.getParams().length == 0)
                    {
                        instanceIdentifier = null;
                    }
                    else
                    {
                        //TODO: Handle "params":[null] case. causes a crash here.
                        instanceIdentifier = (String) request.getParams()[0];
                    }
                }
            }
            break;
        }


    }

    //Getters and Setters
    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    public WisperCallType getCallType()
    {
        String fullMethodName = getFullMethodName();
        if (fullMethodName != null)
        {
            ArrayList<String> components = new ArrayList<String>(Arrays.asList(fullMethodName.split(":")));
            String lastComponent = components.get(components.size() - 1);

            if (components.size() > 1)
            {
                //Cases like ObjectName:~
                if (lastComponent.contains(DESTRUCT_METHOD_NAME))
                {
                    return DESTROY_INSTANCE;
                }
                //Cases like ObjectName:!
                else if (lastComponent.contains("!"))
                {
                    return INSTANCE_EVENT;
                }
                //Cases like X:x
                else
                {
                    return INSTANCE_METHOD;
                }
            }

            //Cases like X~
            if (lastComponent.contains(CONSTRUCT_METHOD_NAME))
            {
                return CREATE_INSTANCE;
            }

            //Cases like X!
            if (lastComponent.contains("!"))
            {
                return STATIC_EVENT;
            }

            //Cases like X.x
            if (lastComponent.contains("."))
            {
                return STATIC_METHOD;
            }
        }

        return UNKNOWN;
    }

    public Request getRequest()
    {
        return request;
    }


    //Public Methods
    public String getClassName()
    {
        String result = null;
        String className = getFullMethodName();
        if (className != null)
        {
            switch (getCallType())
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

    public String getMethodName()
    {
        String result = null;
        String fullMethodName = getFullMethodName();
        if (fullMethodName != null)
        {
            ArrayList<String> classComponents = new ArrayList<String>(Arrays.asList(fullMethodName.split("\\.")));
            String lastComponent = classComponents.get(classComponents.size() - 1);
            switch (getCallType())
            {

                case UNKNOWN:
                    break;
                case CREATE_INSTANCE:
                    result = CONSTRUCT_METHOD_NAME;
                    break;
                case DESTROY_INSTANCE:
                    result = DESTRUCT_METHOD_NAME;
                    break;
                case STATIC_METHOD:
                    result = lastComponent;
                    break;
                case STATIC_EVENT:
                    break;
                case INSTANCE_METHOD:
                {
                    ArrayList<String> components = new ArrayList<String>(Arrays.asList(lastComponent.split(":")));
                    result = components.get(components.size() - 1);
                }
                break;
                case INSTANCE_EVENT:
                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public Object[] getParams()
    {
        Object[] parameters = null;
        int index = 0;

        switch (getCallType())
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

        if (request != null)
        {
            parameters = request.getParams();
        }

        if (notification != null)
        {
            parameters = notification.getParams();
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

    public String getFullMethodName()
    {
        if (notification != null)
        {
            return notification.getMethodName();
        }

        if (request != null)
        {
            return request.getMethodName();
        }

        return null;
    }

    public Notification getNotification()
    {
        return notification;
    }
}
