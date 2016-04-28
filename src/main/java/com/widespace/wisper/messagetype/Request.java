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

    private final String method;
    private final Object[] params;


    public Request(@NotNull JSONObject json) throws JSONException
    {
        if (json.has(Constants.ID))
        {
            identifier = json.getString(Constants.ID);
        }

        method = json.getString(Constants.METHOD);

        params = (Object[]) deserialize(json.getJSONArray(Constants.PARAMS));
    }

    public Request(@NotNull JSONObject json, ResponseBlock block) throws JSONException
    {
        this(json);
        this.responseBlock = block;
    }

    /**
     * Shorthand to create a Request with a method, but without any response block or parameters.
     * @param method
     */
    public Request(String method)
    {
        this(method, null);
    }

    /**
     * Create a Request with a method, a response block and a set of parameters.
     * @param method
     * @param block
     * @param params
     */
    public Request(String method, ResponseBlock block, Object ...params) {

        this.method = method;
        this.responseBlock = block;
        this.params = params;
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

    public Object[] getParams()
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
        jsonObject.put(Constants.METHOD, method == null ? "" : method);
        jsonObject.put(Constants.PARAMS, params == null ? new String[]{} : (JSONArray) serialize(params));

        return jsonObject;
    }
}
