package com.widespace.wisper.messagetype;

import com.widespace.wisper.controller.ResponseBlock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request object that you can either use yourself to request the other RPC endpoint or you will get from the RPC controller when the other endpoint is asking you for it.
 * The WSRPCRequest is a subclass of AbstractMessage.
 *
 * @see Notification
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Request extends AbstractMessage
{

    private ResponseBlock responseBlock;

    private String identifier;
    private String method;
    private Object[] params;


    public Request()
    {
        super();
    }

    public Request(JSONObject json) throws JSONException
    {
        if (json == null)
        {
            return;
        }

        if (json.has("id"))
        {
            identifier = json.getString("id");
        }

        if (json.has("method"))
        {
            method = json.getString("method");
        }

        if (json.has("params"))
        {
            params = (Object[]) deserialize(json.getJSONArray("params"));
        }
    }

    public Request(JSONObject json, ResponseBlock block) throws JSONException
    {
        this(json);
        this.responseBlock = block;
    }


    public ResponseBlock getResponseBlock()
    {
        return responseBlock;
    }

    public void setResponseBlock(ResponseBlock responseBlock)
    {
        this.responseBlock = responseBlock;
    }

    @Override
    public RPCMessageType type()
    {
        return RPCMessageType.REQUEST;
    }


    /**
     * The id of this request used to identify what response is paired with what request. A response to this request must have the exact same requestIdentifier.
     *
     * @return identifier of the request
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Setter for id of this request used to identify what response is paired with what request. A response to this request must have the exact same requestIdentifier.
     *
     * @param identifier desired Id
     */
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String methodName) throws JSONException
    {
        this.method = methodName;
    }

    public Object[] getParams()
    {
        return params;
    }

    public void setParams(Object[] params) throws JSONException
    {
        this.params = params;
    }

    /**
     * Creates a response object for you to pass to the responseBlock if you are responding to a request. This response object will have the requestIdentifier already set correctly.
     * A response to this request must have the exact same requestIdentifier.
     *
     * @return Response with the same ID and
     * @throws JSONException
     */
    public Response createResponse() throws JSONException
    {
        return new Response(this);
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", identifier == null ? "" : identifier);
        jsonObject.put("method", method == null ? "" : method);
        jsonObject.put("params", params == null ? new String[]{} : (JSONArray) serialize(params));

        return jsonObject;
    }
}