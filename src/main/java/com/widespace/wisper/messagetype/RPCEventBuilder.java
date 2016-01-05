package com.widespace.wisper.messagetype;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Ehssan Hoorvash on 27/06/14.
 */
public class RPCEventBuilder
{
    private String methodName;
    private String instanceIdentifier;
    private String name;
    private Object value;


    public Event buildStaticEvent()
    {
        methodName += "!";
        return new Event(methodName, instanceIdentifier, name, value);
    }

    public Event buildInstanceEvent()
    {
        methodName += ":!";
        return new Event(methodName, instanceIdentifier, name, value);
    }

    public RPCEventBuilder withMethodName(String methodName)
    {
        this.methodName = methodName;
        return this;
    }

    public RPCEventBuilder withInstanceIdentifier(String instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier;
        return this;
    }

    public RPCEventBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public RPCEventBuilder withValue(Object value)
    {
        if (value!=null && value instanceof Map)
        {
            this.value = new JSONObject((Map) value);
        }
        else
        {
            this.value = value;
        }
        return this;
    }


}
