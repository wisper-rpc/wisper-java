package com.widespace.wisper.messagetype.error;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.RPCMessageType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RPCError extends AbstractMessage
{
    public static String RPC_ANDROID_ERROR_DOMAIN = "20";
    private int code;
    private int domain;
    private String name;
    private String message;

    private JSONObject data;
    private RPCError underlying;
    private String id;

    public RPCError(RPCErrorBuilder builder)
    {
        this.id = builder.getId();
        this.code = builder.getCode();
        this.domain = builder.getDomain().getDomainCode();
        this.name = builder.getName();
        this.message = builder.getMessage();
        this.data = builder.getData();
        this.underlying = builder.getUnderlyingError();
    }

    public RPCError(JSONObject jsonObject)
    {
        this.jsonForm = jsonObject;
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.ERROR;
    }

    @Override
    public String toJsonString()
    {
        JSONObject errorJson = null;
        try
        {
            errorJson = new JSONObject();
            errorJson.put("id", id);
            JSONObject errorFields = new JSONObject();
            errorFields.put("code", code);
            errorFields.put("name", name);
            errorFields.put("domain", domain);
            errorFields.put("message", message);
            errorFields.put("data", data);
            if (underlying != null)
            {
                errorFields.put("underlying", new JSONObject(underlying.toJsonString())); //recursion check?
            }
            errorJson.put("error", errorFields);

        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return errorJson.toString();
    }
}
