package com.widespace.wisper.messagetype;

import java.util.ArrayList;


/**
 * Model object representing an event to be sent or received.
 * <p>
 * Created by Ehssan Hoorvash on 17/06/14.
 */
public class Event extends Notification
{
    private String instanceIdentifier;
    private String name;
    private Object value;

    public Event()
    {
        super();
    }

    public Event(String methodName, String instanceIdentifier, String name, Object value)
    {
        this.methodName = methodName;
        this.instanceIdentifier = instanceIdentifier;
        this.name = name;
        this.value = value;
    }

    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    public void setInstanceIdentifier(String instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    @Override
    public Object[] getParams()
    {
        ArrayList<Object> params = new ArrayList<Object>();
        if (instanceIdentifier != null)
        {
            params.add(instanceIdentifier);
        }

        params.add(name == null ? "" : name);
        params.add(value == null ? "" : value);

        return params.toArray(new Object[params.size()]);
    }


}
