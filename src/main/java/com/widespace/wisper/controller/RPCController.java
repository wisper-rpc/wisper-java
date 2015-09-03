package com.widespace.wisper.controller;


import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.ErrorDomain;
import com.widespace.wisper.messagetype.error.RPCError;
import com.widespace.wisper.messagetype.error.RPCErrorBuilder;
import com.widespace.wisper.messagetype.error.RPCErrorCodes;
import com.widespace.wisper.utils.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

/**
 * RPCController is the receiving end point for RPC messages coming from a
 * WebView end point. The controller will handle the incoming objects and parses
 * them into a model objects that are easier to interact with.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */

public class RPCController
{
    public static final String EXTRA_KEY_ADSPACE = "adspace";
    public static final String EXTRA_KEY_WEBVIEW = "webview";

    protected RPCControllerCallback callback;
    private HashMap<String, RPCRequest> requests;

    private HashMap<String, Object> extras;

    public RPCController(RPCControllerCallback callback)
    {
        this.callback = callback;
        requests = new HashMap<String, RPCRequest>();
        extras = new HashMap<String, Object>();
    }

    /**
     * created a unique identifier in form of a UUID for the request
     *
     * @return a new UUID everytime it is called
     */
    public String uniqueRequestIdentifier()
    {
        return UUID.randomUUID().toString();
    }

    public void setExtra(String key, Object value)
    {
        extras.put(key, value);
    }

    public Object getExtra(String key)
    {
        return extras.get(key);
    }

    public HashMap getExtras()
    {
        return extras;
    }


    /**
     * Entry point for the RPC interface from the WebView. Decides if we can
     * handle this request or not, will also handle the request. This is the
     * only method you need to call from the webView delegate to handle RPC
     * Communication.
     *
     * @param jsonString a string representation of the message, the message is
     *                   a JSON object and if it is malformed an error will be returned
     *                   back to the endpoint in form of an RPCError.
     */
    public void handle(String jsonString)
    {
        try
        {
            System.out.println(" --------------> " + jsonString);
            handle(new JSONObject(jsonString));
        }
        catch (JSONException e)
        {
            RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.PARSE_ERROR.getErrorCode()).withMessage(e.getMessage()).build();
            respondToRequest(err);
        }
    }

    private void handle(JSONObject rpcReq) throws JSONException
    {
        RPCMessageType messageType = determineMessageType(rpcReq);
        switch (messageType)
        {
            case UNKNOWN:
                RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.GENERIC_ERROR.getErrorCode()).withMessage("Request Type is unknown.").withId(getIdFromJson(rpcReq)).build();
                respondToRequest(err);
                break;
            case REQUEST:
                handleRequestMessageType(rpcReq);
                break;
            case RESPONSE:
                handleResponseMessageType(rpcReq);
                break;
            case NOTIFICATION:
                handleNotificationMessageType(rpcReq);
                break;
            case ERROR:
                handleErrorMessageType(rpcReq);
                break;
        }
    }

    /**
     * Send a message to the other endpoint with a message type
     *
     * @param message the message to be sent to the other endpoint.
     */
    public void respondToRequest(RPCAbstractMessage message)
    {
        try
        {
            switch (message.type())
            {
                case UNKNOWN:
                    RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.FORMAT_ERROR.getErrorCode()).withMessage("Message type not recognized for " + message.toJsonString()).build();
                    respondWithError(err);
                    break;
                case REQUEST:
                    makeRequest((RPCRequest) message);
                    break;
                case RESPONSE:
                    respondWithResponse((RPCResponse) message);
                    break;
                case NOTIFICATION:
                    respondWithNotification((RPCNotification) message);
                    break;
                case ERROR:
                    respondWithError((RPCError) message);
                    break;
            }
        }
        catch (JSONException e)
        {
            // ignore!
        }
    }

    private void handleNotificationMessageType(JSONObject rpcReq) throws JSONException
    {
        RPCNotification notification = new RPCNotification(rpcReq, callback);
        handleRPCNotification(notification);

    }

    protected void handleRPCNotification(RPCNotification notification)
    {
        notification.handle();
    }

    private void handleRequestMessageType(JSONObject rpcReq) throws JSONException
    {
        RPCRequest request = new RPCRequest(rpcReq, new ResponseBlock()
        {
            @Override
            public void perform(RPCResponse response)
            {
                System.out.println("perform on response callback called");
                passMessageToCallback(response.toJsonString());
            }
        });

        handleRPCRequest(request);
    }

    protected void handleRPCRequest(RPCRequest request)
    {
        callback.rpcControllerReceivedRequest(request);
    }

    private void handleResponseMessageType(JSONObject rpcResponse) throws JSONException
    {
        String requestId = getIdFromJson(rpcResponse);
        if (requests != null && requests.containsKey(requestId))
        {
            RPCRequest theRequest = requests.get(requestId);
            requests.remove(requestId);
            RPCResponse response = new RPCResponse(rpcResponse, theRequest);
            if (theRequest.getResponseBlock() != null)
            {
                theRequest.getResponseBlock().perform(response);
            }
        }
    }

    private void handleErrorMessageType(JSONObject rpcError) throws JSONException
    {
        // Run response through WSRPCRequest object if it invoked the request
        String requestId = getIdFromJson(rpcError);
        if (requestId != null && requests.containsKey(requestId))
        {
            RPCRequest theRequest = requests.get(requestId);
            requests.remove(requestId);
            theRequest.getResponseBlock().perform(theRequest.createResponse());
        }

        RPCError error = new RPCError(rpcError);
        callback.rpcControllerReceivedError(error);
    }

    // Handlers of outgoing messages

    // Handlers of outgoing messages
    public void makeRequest(RPCRequest request) throws JSONException
    {
        if (request.getIdentifier() == null)
        {
            request.setIdentifier(uniqueRequestIdentifier());
        }

        requests.put(request.getIdentifier(), request);
        passMessageToCallback(request.toJsonString());
    }

    private void respondWithResponse(RPCResponse response)
    {
        passMessageToCallback(response.toJsonString());
    }

    private void respondWithNotification(RPCNotification notification)
    {
        passMessageToCallback(notification.toJsonString());
    }

    private void respondWithError(RPCError rpcError)
    {
        // format json
        String formatted = rpcError.toJsonString();
        passMessageToCallback(formatted);


    }

    private void passMessageToCallback(String jsonString)
    {
        String escapedJson = StringEscapeUtils.escapeJavaScript(jsonString);
        System.out.println("<------------" + escapedJson);
        callback.rpcControllerGeneratedMessage(escapedJson);
    }


    // Utility
    private RPCMessageType determineMessageType(JSONObject rpcRequest)
    {
        RPCMessageType result = RPCMessageType.UNKNOWN;

        if (rpcRequest.has("method") && rpcRequest.has("params"))
        {
            if (rpcRequest.has("id"))
            {
                result = RPCMessageType.REQUEST;
            }
            else
            {
                result = RPCMessageType.NOTIFICATION;
            }
        }
        else if (rpcRequest.has("result") && rpcRequest.has("id"))
        {
            result = RPCMessageType.RESPONSE;
        }
        else if (rpcRequest.has("error"))
        {
            result = RPCMessageType.ERROR;
        }

        return result;
    }

    private String getIdFromJson(JSONObject json) throws JSONException
    {
        if (json.has("id"))
        {
            return json.getString("id");
        }

        return null;
    }


}
