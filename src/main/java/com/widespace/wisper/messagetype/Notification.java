package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Notification extends AbstractMessage
{
    protected String methodName;
    protected Object[] params;

    public Notification(String methodName, Object ...params)
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

        if (jsonObject.has(Constants.METHOD))
        {
            this.methodName = jsonObject.getString(Constants.METHOD);
        }

        if (jsonObject.has(Constants.PARAMS))
        {
            params = (Object[]) deserialize(jsonObject.getJSONArray(Constants.PARAMS));
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

    public Object[] getParams()
    {
        return params;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.METHOD, methodName == null ? "" : methodName);
        jsonObject.put(Constants.PARAMS, getParams() == null ? "" : serialize(getParams()));

        return jsonObject;
    }
}
