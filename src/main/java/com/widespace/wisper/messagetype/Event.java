package com.widespace.wisper.messagetype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Model object representing an event to be sent or received.
 * <p/>
 * Created by Ehssan Hoorvash on 17/06/14.
 */
public class Event extends Notification
{
    @Nullable
    private final String instanceIdentifier;

    @NotNull
    private final String name;

    @Nullable
    private final Object value;

    public Event(@NotNull String methodName, @NotNull String name, @Nullable Object value)
    {
        super(methodName, new Object[]{name, value});

        if (!methodName.endsWith("!"))
        {
            throw new IllegalArgumentException("Event method names must end with '!'.");
        }

        if (methodName.endsWith(":!"))
        {
            throw new IllegalArgumentException("Static Event method names mustn't end with ':!'.");
        }

        this.instanceIdentifier = null;
        this.name = name;
        this.value = value;
    }

    public Event(@NotNull String methodName, @NotNull String id, @NotNull String name, @Nullable Object value)
    {
        super(methodName, new Object[]{id, name, value});

        if (!methodName.endsWith(":!"))
        {
            throw new IllegalArgumentException("Instance Event method names must end with ':!'.");
        }

        this.instanceIdentifier = id;
        this.name = name;
        this.value = value;
    }

    public Event(@NotNull String methodName, @NotNull Object[] params)
    {
        super(methodName, params);

        boolean staticType = methodName.endsWith("!");
        boolean instanceType = staticType && methodName.endsWith(":!");

        // Check for ':!' first, so we don't attempt process a '!' event by mistake.
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

    public Event(@NotNull Notification notification)
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
