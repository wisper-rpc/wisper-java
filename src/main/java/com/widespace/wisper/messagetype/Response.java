package com.widespace.wisper.messagetype;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * When an instance handling an Request has finished, it should generate an instance of this object and fill it with the results.
 * This object is then passed to the other endpoint either through the Response's responseBlock or through the Gateway.
 * <p>
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Response extends AbstractMessage
{
    private String identifier;
    private Object result;


    public Response()
    {
        this(null, null);
    }

    public Response(String identifier, Object result)
    {
        this.identifier = identifier;
        this.result = result;
    }

    public Response(Request theRequest)
    {
        this.identifier = theRequest.getIdentifier();
    }

    public Response(JSONObject json)
    {
        if (json == null)
        {
            return;
        }

        if (json.has("id"))
        {
            this.identifier = json.getString("id");
        }

        if (json.has("result"))
        {
            this.result = deserialize(json.get("result"));
        }
    }

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public Object getResult()
    {
        return result;
    }

    /**
     * Sets the desired result in this response.
     * The results could be of any serializable type.
     *
     * @param newResult the new result
     * @throws JSONException if the newResult could not be parsed properly into the result, this exception is thrown
     */
    public void setResult(Object newResult)
    {
        this.result = newResult;
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.RESPONSE;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", identifier == null ? "" : identifier);
        jsonObject.put("result", result == null ? "" : serialize(result));

        return jsonObject;
    }
}
