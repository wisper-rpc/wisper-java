package com.widespace.wisper.messagetype;

import com.widespace.wisper.controller.ResponseBlock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Request object that you can either use yourself to request the other RPC endpoint or you will get from the RPC controller when the other endpoint is asking you for it.
 * The WSRPCRequest is a subclass of RPCAbstractMessage.
 *
 * @see com.widespace.wisper.messagetype.RPCNotification
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RPCRequest extends RPCAbstractMessage
{

    private ResponseBlock responseBlock;

    private String identifier;
    private String methodName;
    private Object[] params;


    public RPCRequest()
    {
        this.jsonForm = new JSONObject();
    }

    public RPCRequest(JSONObject jsonRequest,  ResponseBlock block) throws JSONException
    {
        this.jsonForm = jsonRequest;
        this.responseBlock = block;

        if (jsonRequest != null && jsonRequest.has("id"))
        {
            this.identifier = jsonRequest.getString("id");
        }

        determineMethodNameAndParameters();
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

    @Override
    public String toJsonString()
    {
        return jsonForm.toString();
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
        try
        {
            this.jsonForm.put("id", identifier);
            this.identifier = identifier;
        }
        catch (JSONException e)
        {

            e.printStackTrace();
        }
    }

    /**
     * Creates a response object for you to pass to the responseBlock if you are responding to a request. This response object will have the requestIdentifier already set correctly.
     * A response to this request must have the exact same requestIdentifier.
     *
     * @return RPCResponse with the same ID and
     * @throws JSONException
     */
    public RPCResponse createResponse() throws JSONException
    {
        JSONObject responseJson = new JSONObject();
        responseJson.put("id", this.identifier);
        responseJson.put("result", new JSONArray());
        return new RPCResponse(responseJson, this);
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName) throws JSONException
    {
        this.methodName = methodName;
        jsonForm.put("method", methodName);
    }

    public void setParams(Object[] params) throws JSONException
    {
        this.params = params;
        jsonForm.put("params" , new JSONArray(Arrays.asList(params)));
    }

    protected void determineMethodNameAndParameters() throws JSONException
    {
        if (this.jsonForm.has("method"))
        {
            this.methodName = this.jsonForm.getString("method");
        }

        if (this.jsonForm.has("params"))
        {
            params = jsonArrayToArray(jsonForm.getJSONArray("params"));
        }
    }

    public Object[] getParams()
    {
        return params;
    }


}