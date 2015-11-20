package com.widespace.wisper.messagetype.error;

import org.json.JSONObject;

import static com.widespace.wisper.base.Constants.*;

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
        if (jsonObject.has(DOMAIN))
        {
            this.domain = jsonObject.getInt(DOMAIN);
        }

        if (jsonObject.has(CODE))
        {
            this.code = jsonObject.getInt(CODE);
        }

        if (jsonObject.has(MESSAGE))
        {
            this.message = jsonObject.getString(MESSAGE);
        }

        if (jsonObject.has(NAME))
        {
            this.name = jsonObject.getString(NAME);
        }

        if (jsonObject.has(DATA))
        {
            this.data = jsonObject.get(DATA);
        }

        if (jsonObject.has(UNDERLYING_ERROR))
        {
            this.underlyingError = new RPCError(jsonObject.getJSONObject(UNDERLYING_ERROR));
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
