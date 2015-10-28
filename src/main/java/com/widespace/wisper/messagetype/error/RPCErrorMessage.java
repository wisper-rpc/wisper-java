package com.widespace.wisper.messagetype.error;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.RPCMessageType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RPCErrorMessage extends AbstractMessage
{

    private String id;
    private RPCError error;

    public RPCErrorMessage()
    {
        this(null, null);
    }


    public RPCErrorMessage(String id, RPCError error)
    {
        this.id = id;
        this.error = error;
    }

    public RPCErrorMessage(JSONObject json)
    {
        if (json.has("id"))
        {
            this.id = json.getString("id");
        }
        if (json.has("error"))
        {
            this.error = new RPCError(json.getJSONObject("error"));
        }
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.ERROR;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public RPCError getError()
    {
        return error;
    }

    public void setError(RPCError error)
    {
        this.error = error;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        if (id != null)
        {
            jsonObject.put("id", id);
        }

        jsonObject.put("error", serialize(error));

        return jsonObject;
    }


}
