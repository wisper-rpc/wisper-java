package com.widespace.wisper.messagetype;

import com.widespace.wisper.controller.RPCRemoteObjectCall;
import com.widespace.wisper.controller.RPCRemoteObjectCallType;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;


/**
 * Model object representing an event to be sent or received.
 * <p/>
 * Created by Ehssan Hoorvash on 17/06/14.
 */
public class RPCEvent extends RPCNotification
{
    private String methodName;
    private String instanceIdentifier;
    private String name;
    private Object value;

    public RPCEvent(String methodName, String instanceIdentifier, String name, Object value) throws JSONException
    {
        this.methodName = methodName;
        this.instanceIdentifier = instanceIdentifier;
        this.name = name;
        this.value = value;

        fillJsonForm(methodName, instanceIdentifier, name, value);
    }

    public RPCEvent(RPCRemoteObjectCall remoteObjectCall) throws JSONException
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
                // theName = (String) (parameters.size() > 1 ? parameters.get(1) : null);
                // theValue = (parameters.size() > 2 ? parameters.get(2) : null);
                theName = (String) parameters.get(0);
                theValue = (parameters.size() > 1) ? parameters.get(1) : null;
            }
            break;
            default:
                break;
        }

        String theMethodName = remoteObjectCall.getMethodName();
        this.methodName = theMethodName;
        String theIdentifier = remoteObjectCall.getInstanceIdentifier();
        this.instanceIdentifier = theIdentifier;

        this.name = theName;
        this.value = theValue;

        fillJsonForm(theMethodName, theIdentifier, theName, theValue);
    }

    private void fillJsonForm(String methodName, String instanceIdentifier, String name, Object value) throws JSONException
    {
        jsonForm.put("method", methodName);
        JSONArray paramsArray = new JSONArray();
        paramsArray.put(instanceIdentifier);
        paramsArray.put(name);
        paramsArray.put(value); //TODO: investigate if this works as intended
        jsonForm.put("params", paramsArray);
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }
}