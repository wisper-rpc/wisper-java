package com.widespace.wisper.messagetype;

import org.jetbrains.annotations.NotNull;

/**
 * An CallMessage is a message that has a method name and a set of parameters.
 * Both {@code Request}s and {@code Notification}s are examples of Invocations.
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
}
