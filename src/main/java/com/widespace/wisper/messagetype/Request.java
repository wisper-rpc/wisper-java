package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.controller.ResponseBlock;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Request object that you can either use yourself to request the other RPC endpoint or you will get from the RPC controller when the other endpoint is asking you for it.
 * The WSRPCRequest is a subclass of AbstractMessage.
 *
 * @see CallMessage
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class Request extends CallMessage
{
    private ResponseBlock responseBlock;
    private String identifier;

    public Request(@NotNull String method)
    {
        this(method, EMPTY_PARAMS);
    }

    /**
     * Create a Request with a method and a set of parameters.
     *
     * @param method
     * @param params
     */
    public Request(@NotNull String method, @NotNull Object[] params)
    {
        super(method, params);
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
    public boolean equals(Object o)
    {
        if (!(o instanceof Request))
        {
            return false;
        }

        Request other = (Request) o;

        return other.method.equals(method) && Arrays.deepEquals(other.params, params) && other.responseBlock == responseBlock;
    }

    @Override
    public JSONObject toJson() throws JSONException
    {
        final JSONObject object = super.toJson();

        object.put(Constants.ID, identifier);

        return object;
    }
}
