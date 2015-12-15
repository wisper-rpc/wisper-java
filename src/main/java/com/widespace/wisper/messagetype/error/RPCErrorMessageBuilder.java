package com.widespace.wisper.messagetype.error;

import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 03/06/14.
 */
public class RPCErrorMessageBuilder
{
    private RPCError error;
    private String id;


    public RPCErrorMessageBuilder(ErrorDomain errorDomain, int errorCode)
    {
        error = new RPCError();
        error.setCode(errorCode);
        error.setDomain(errorDomain.getDomainCode());

    }


    public RPCErrorMessageBuilder withData(JSONObject data)
    {
        error.setData(data);
        return this;
    }

    public RPCErrorMessageBuilder withMessage(String message)
    {
        error.setMessage(message);
        return this;
    }

    public RPCErrorMessageBuilder withName(String name)
    {
        error.setName(name);
        return this;
    }

    public RPCErrorMessageBuilder withUnderlyingError(RPCError underlying)
    {
        error.setUnderlyingError(underlying);
        return this;
    }

    public RPCErrorMessageBuilder withId(String id)
    {
        this.id = id;
        return this;
    }

    public RPCErrorMessage build()
    {
        return new RPCErrorMessage(id, error);
    }
}
