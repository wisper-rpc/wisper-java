package com.widespace.wisper.messagetype;

import org.jetbrains.annotations.NotNull;


/**
 * Model object representing an event to be sent or received.
 * <p/>
 * Created by Ehssan Hoorvash on 17/06/14.
 */
public class Event extends Notification
{
    private final String instanceIdentifier;

    @NotNull
    private final String name;

    private final Object value;


    public Event(String methodName, @NotNull Object... params)
    {
        super(methodName, params);

        boolean staticType = methodName.endsWith("!");
        boolean instanceType = staticType && methodName.endsWith(":!");

        if (instanceType)
        {
            if (params.length != 3)
            {
                throw new IllegalArgumentException("Instance events must have 3 parameters.");
            }

            instanceIdentifier = (String) params[0];
            name = (String) params[1];
            value = params[2];
        }
        else if (staticType)
        {
            if (params.length != 2)
            {
                throw new IllegalArgumentException("Static events must have 2 parameters.");
            }

            instanceIdentifier = null;
            name = (String) params[0];
            value = params[1];
        }
        else
        {
            throw new IllegalArgumentException("Event method names require '!' or ':!' suffix.");
        }
    }

    public Event(Notification notification)
    {
        this(notification.getMethodName(), notification.getParams());
    }

    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }
}
