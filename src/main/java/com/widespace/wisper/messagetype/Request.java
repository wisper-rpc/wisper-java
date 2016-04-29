package com.widespace.wisper.messagetype;

import org.jetbrains.annotations.NotNull;
import com.widespace.wisper.base.Constants;
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

    @NotNull
    private final String method;

    @NotNull
    private final Object[] params;

    /**
     * Create a Request with a method and an optional set of parameters.
     *
     * @param method
     * @param params
     */
    public Request(String method, Object... params)
    {

        this.method = method;
        this.params = params;
    }

    public Request(@NotNull JSONObject json) throws JSONException
    {
        this(json.getString(Constants.METHOD), jsonArrayToArray(json.getJSONArray(Constants.PARAMS)));

        if (json.has(Constants.ID))
        {
            identifier = json.getString(Constants.ID);
        }
    }

    public Request(@NotNull JSONObject json, ResponseBlock block) throws JSONException
    {
        this(json);
        this.responseBlock = block;
    }

    public Request withResponseBlock(ResponseBlock block)
    {
        responseBlock = block;
        return this;
    }

    public void setResponseBlock(ResponseBlock block)
    {
        responseBlock = block;
    }


    public ResponseBlock getResponseBlock()
    {
        return responseBlock;
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

    public String getMethodName()
    {
        return method;
    }

    public
    @NotNull
    Object[] getParams()
    {
        return params;
    }

    /**
     * Creates a response object for you to pass to the responseBlock if you are responding to a request. This response object will have the requestIdentifier already set correctly.
     * A response to this request must have the exact same requestIdentifier.
     *
     * @return Response with the same ID and
     */
    public Response createResponse()
    {
        return new Response(this);
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.ID, identifier == null ? "" : identifier);
        jsonObject.put(Constants.METHOD, method);
        jsonObject.put(Constants.PARAMS, (JSONArray) serialize(params));

        return jsonObject;
    }
}
