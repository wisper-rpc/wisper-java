package com.widespace.wisper.messagetype.error;

import org.json.JSONObject;

/**
 * Created by ehssanhoorvash on 21/10/15.
 */
public class RPCError
{
    private int code;
    private int domain;
    private String name;
    private String message;

    private Object data;
    private RPCError underlyingError;

    public RPCError()
    {
        domain = ErrorDomain.ANDROID.getDomainCode();
    }

    public RPCError(JSONObject jsonObject)
    {
        if (jsonObject.has("domain"))
        {
            this.domain = jsonObject.getInt("domain");
        }

        if (jsonObject.has("code"))
        {
            this.code = jsonObject.getInt("code");
        }

        if (jsonObject.has("message"))
        {
            this.message = jsonObject.getString("message");
        }

        if (jsonObject.has("name"))
        {
            this.name = jsonObject.getString("name");
        }

        if (jsonObject.has("data"))
        {
            this.data = jsonObject.get("data");
        }

        if (jsonObject.has("underlying"))
        {
            this.underlyingError = new RPCError(jsonObject.getJSONObject("underlyingError"));
        }
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public int getDomain()
    {
        return domain;
    }

    public void setDomain(int domain)
    {
        this.domain = domain;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public RPCError getUnderlyingError()
    {
        return underlyingError;
    }

    public void setUnderlyingError(RPCError underlyingError)
    {
        this.underlyingError = underlyingError;
    }


}
