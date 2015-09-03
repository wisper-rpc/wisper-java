package com.widespace.wisper.messagetype.error;

import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 03/06/14.
 */
public class RPCErrorBuilder
{
    private int code;
    private String name;
    private String message;
    private ErrorDomain domain;

    private JSONObject data;
    private RPCError underlying;
    private String id;

    public RPCErrorBuilder(ErrorDomain errorDomain, int errorCode)
    {
        this.domain = errorDomain;
        this.code = errorCode;
        this.name = "";
    }


    public RPCError build()
    {
        return new RPCError(this);
    }

    public RPCErrorBuilder withData(JSONObject data)
    {
        this.data = data;
        return this;
    }

    public RPCErrorBuilder withMessage(String message)
    {
        this.message = message;
        return this;
    }

    public RPCErrorBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public RPCErrorBuilder withUnderlyingError(RPCError underlying)
    {
        this.underlying = underlying;
        return this;
    }

    public int getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    public String getMessage()
    {
        return message;
    }

    public JSONObject getData()
    {
        return data;
    }

    public RPCError getUnderlyingError()
    {
        return underlying;
    }

    public RPCErrorBuilder withId(String id)
    {
        this.id = id;
        return this;
    }

    public String getId()
    {
        return id;
    }

    public ErrorDomain getDomain()
    {
        return domain;
    }
}
