package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Notification extends Invocation
{
    public Notification(@NotNull String methodName)
    {
        super(methodName);
    }

    public Notification(String methodName, Object[] params)
    {
        super(methodName, params);
    }

    public Notification(JSONObject jsonObject) throws JSONException
    {
        super(jsonObject.getString(Constants.METHOD), (Object[]) deserialize(jsonObject.getJSONArray(Constants.PARAMS)));
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Notification))
        {
            return false;
        }

        Notification other = (Notification) o;

        return other.method.equals(method) && Arrays.deepEquals(other.params, params);
    }

    @Override
    public int hashCode()
    {
        return method.hashCode() ^ Arrays.hashCode(params);
    }


    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.NOTIFICATION;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        return new JSONObject()
        {
            {
                put(Constants.METHOD, method);
                put(Constants.PARAMS, params);
            }
        };
    }
}
