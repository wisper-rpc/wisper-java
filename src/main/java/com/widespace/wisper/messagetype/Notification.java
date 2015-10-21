package com.widespace.wisper.messagetype;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Notification extends AbstractMessage
{
    protected String methodName;
    protected Object[] params;

    public Notification()
    {
        this(null, null);
    }

    public Notification(String methodName, Object[] params)
    {
        this.methodName = methodName;
        this.params = params;
    }

    public Notification(JSONObject jsonObject) throws JSONException
    {
        if (jsonObject == null)
        {
            return;
        }

        if (jsonObject.has("method"))
        {
            this.methodName = jsonObject.getString("method");
        }

        if (jsonObject.has("params"))
        {
            params = (Object[]) deserialize(jsonObject.getJSONArray("params"));
        }
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.NOTIFICATION;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    public Object[] getParams()
    {
        return params;
    }

    public void setParams(Object[] params)
    {
        this.params = params;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", methodName == null ? "" : methodName);
        jsonObject.put("params", getParams() == null ? "" :  serialize(getParams()));

        return jsonObject;
    }
}
