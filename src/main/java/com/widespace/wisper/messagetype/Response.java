package com.widespace.wisper.messagetype;


import com.widespace.wisper.utils.ClassUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When an instance handling an Request has finished, it should generate an instance of this object and fill it with the results.
 * This object is then passed to the other endpoint either through the Response's responseBlock or through the RPCController.
 * <p/>
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Response extends AbstractMessage
{
    private String identifier;

    public Response(JSONObject jsonRpcResponse, Request theRequest) throws JSONException
    {
        this.jsonForm = jsonRpcResponse;
        setIdentifier(theRequest.getIdentifier());
    }

    public Response(Request theRequest) throws JSONException
    {
        this(new JSONObject(), theRequest);
    }

    public Response(JSONObject json)
    {
        this.jsonForm = json;
    }


    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.RESPONSE;
    }

    @Override
    public String toJsonString()
    {
        return jsonForm.toString();
    }

    public Object getResult() throws JSONException
    {
        return jsonForm.get("result");
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier) throws JSONException
    {
        this.identifier = identifier;
        jsonForm.put("id", identifier);
    }

    /**
     * Sets the desired result in this response.
     * The results could be of any serializable type.
     *
     * @param newResult the new result
     * @throws JSONException if the newResult could not be parsed properly into the result, this exception is thrown
     */
    public void setResult(Object newResult) throws JSONException
    {
        jsonForm.put("result", serialize(newResult));
    }

    private Object serialize(Object newResult)
    {
        if (newResult.getClass().isArray())
        {
            JSONArray array = new JSONArray();

            for (Object object : (Object[]) newResult)
            {
                array.put(serialize(object));
            }

            return array;
        }
        else if (newResult instanceof List)
        {
            JSONArray array = new JSONArray();
            for (java.lang.Object object : (List) newResult)
            {
                array.put(serialize(object));
            }
            return array;
        }
        else if (ClassUtils.isPrimitive(newResult.getClass()) || newResult.getClass().equals(String.class))
        {
            return newResult;
        }
        else if (newResult.getClass().equals(JSONObject.class) || newResult.getClass().equals(JSONArray.class))
        {
            return newResult;
        }
        else if (newResult.getClass().isAssignableFrom(Map.class) || newResult.getClass().isAssignableFrom(HashMap.class))
        {
            return new JSONObject((Map) newResult);
        }
        else if (Number.class.isAssignableFrom(newResult.getClass()))
        {
            return newResult;
        }
        else
        {
            //Just ignore it for now...the return type must be serializable
            return null;
        }
    }
}
