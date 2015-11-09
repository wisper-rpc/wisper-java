package com.widespace.wisper.messagetype;

import com.widespace.wisper.controller.RemoteObjectCall;
import com.widespace.wisper.controller.RPCRemoteObjectCallType;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    public Event(String methodName, String instanceIdentifier, String name, Object value) throws JSONException
    {
        this.methodName = methodName;
        this.instanceIdentifier = instanceIdentifier;
        this.name = name;
        this.value = value;
    }

    public Event(RemoteObjectCall remoteObjectCall) throws JSONException
    {
        RPCRemoteObjectCallType callType = remoteObjectCall.getCallType();
        List<Object> parameters = Arrays.asList(remoteObjectCall.getParams());

        String theName = null;
        Object theValue = null;

        switch (callType)
        {
            case STATIC_EVENT:
            {
                theName = (String) parameters.get(0);
                theValue = (parameters.size() > 1) ? parameters.get(1) : null;
            }
            break;
            case INSTANCE_EVENT:
            {
                theName = (String) parameters.get(0);
                theValue = (parameters.size() > 1) ? parameters.get(1) : null;
            }
            break;
            default:
                break;
        }

        this.methodName = remoteObjectCall.getMethodName();
        this.instanceIdentifier = remoteObjectCall.getInstanceIdentifier();

        this.name = theName;
        this.value = theValue;
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
