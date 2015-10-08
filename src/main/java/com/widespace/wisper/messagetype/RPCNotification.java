package com.widespace.wisper.messagetype;

import com.widespace.wisper.controller.RPCControllerCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RPCNotification extends RPCAbstractMessage
{
    private String methodName;
    private Object[] params;


    public RPCNotification(JSONObject rpcNotification) throws JSONException
    {
        this.jsonForm = rpcNotification;
        determineMethodNameAndParameters();
    }

    public RPCNotification()
    {
        this.jsonForm = new JSONObject();
    }


    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName) throws JSONException
    {
        this.methodName = methodName;
        jsonForm.put("method", methodName);
    }

    public Object[] getParams()
    {
        return params;
    }

    public void setParams(Object[] params) throws JSONException
    {
        this.params = params;
        jsonForm.put("params" , new JSONArray(Arrays.asList(params)));
    }

    public void setParams(ArrayList<Object> params)
    {
        this.params = params.toArray();
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.NOTIFICATION;
    }

    @Override
    public String toJsonString()
    {
        return jsonForm.toString();
    }

    private void determineMethodNameAndParameters() throws JSONException
    {
        if (this.jsonForm == null)
        {
            return;
        }

        if (this.jsonForm.has("method"))
        {
            this.methodName = this.jsonForm.getString("method");
        }

        if (this.jsonForm.has("params"))
        {
            this.params = jsonArrayToArray(jsonForm.getJSONArray("params"));
        }
    }
}
