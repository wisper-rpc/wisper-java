package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Notification extends CallMessage
{
    public Notification(@NotNull String methodName)
    {
        super(methodName);
    }

    public Notification(@NotNull String methodName, @NotNull Object[] params)
    {
        super(methodName, params);
    }

    public Notification(@NotNull JSONObject jsonObject) throws JSONException
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
    public RPCMessageType type()
    {
        return RPCMessageType.NOTIFICATION;
    }
}
