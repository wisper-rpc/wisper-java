package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * An CallMessage is a message that has a method name and a set of parameters.
 * Both {@code Request}s and {@code Notification}s are examples of CallMessages.
 *
 * Created by oskar on 2016-05-10.
 */
public abstract class CallMessage extends AbstractMessage
{
    @NotNull
    protected final String method;

    @NotNull
    protected final Object[] params;

    protected CallMessage(@NotNull String method)
    {
        this(method, EMPTY_PARAMS);
    }

    protected CallMessage(@NotNull String method, @NotNull Object[] params)
    {
        this.method = method;
        this.params = params;
    }

    @NotNull
    public String getMethodName()
    {
        return method;
    }

    @NotNull
    public Object[] getParams()
    {
        return params;
    }

    @Override
    public int hashCode()
    {
        return method.hashCode() ^ Arrays.hashCode(params);
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        return new JSONObject()
        {
            {
                put(Constants.METHOD, method);
                put(Constants.PARAMS, serialize(params));
            }
        };
    }
}
