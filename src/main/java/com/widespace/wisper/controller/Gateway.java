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
 * Gateway is the receiving end point for RPC messages coming from a
 * WebView end point. The controller will handle the incoming objects and parses
 * them into a model objects that are easier to interact with.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */

public class Gateway
{
    public static final String EXTRA_KEY_ADSPACE = "adspace";
    public static final String EXTRA_KEY_WEBVIEW = "webview";

    protected GatewayCallback callback;
    private HashMap<String, Request> requests;

    private HashMap<String, Object> extras;

    public Gateway(GatewayCallback callback)
    {
        this.callback = callback;
        requests = new HashMap<String, Request>();
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
     * @param rpcMessage a string representation of the message, the message is
     *                   a JSON object and if it is malformed an error will be returned
     *                   back to the endpoint in form of an RPCError.
     */
    public void handle(String rpcMessage)
    {
        try
        {
            System.out.println(" --------------> " + rpcMessage);
            handle(new JSONObject(rpcMessage));
        }
        catch (JSONException e)
        {
            RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.PARSE_ERROR.getErrorCode()).withMessage(e.getMessage()).build();
            respondToRequest(err);
        }
    }

    private void handle(JSONObject json) throws JSONException
    {
        RPCMessageType messageType = new MessageFactory().determineMessageType(json);
        switch (messageType)
        {
            case UNKNOWN:
                RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.GENERIC_ERROR.getErrorCode()).withMessage("Request Type is unknown.").withId(getIdFromJson(json)).build();
                respondToRequest(err);
                break;
            case REQUEST:
                handleRequestMessageType(json);
                break;
            case RESPONSE:
                handleResponseMessageType(json);
                break;
            case NOTIFICATION:
                handleNotificationMessageType(json);
                break;
            case ERROR:
                handleErrorMessageType(json);
                break;
        }
    }

    /**
     * Send a message to the other endpoint with a message type
     *
     * @param message the message to be sent to the other endpoint.
     */
    public void respondToRequest(AbstractMessage message)
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
                    makeRequest((Request) message);
                    break;
                case RESPONSE:
                    respondWithResponse((Response) message);
                    break;
                case NOTIFICATION:
                    respondWithNotification((Notification) message);
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
        Notification notification = new Notification(rpcReq);
        handleRPCNotification(notification);

    }

    protected void handleRPCNotification(Notification notification)
    {
        if (callback != null)
        {
            callback.gatewayReceivedMessage(notification);
        }
    }

    private void handleRequestMessageType(JSONObject rpcReq) throws JSONException
    {
        Request request = new Request(rpcReq, new ResponseBlock()
        {
            @Override
            public void perform(Response response)
            {
                System.out.println("perform on response callback called");
                passMessageToCallback(response.toJsonString());
            }
        });

        handleRPCRequest(request);
    }

    protected void handleRPCRequest(Request request)
    {
        callback.gatewayReceivedMessage(request);
    }

    private void handleResponseMessageType(JSONObject rpcResponse) throws JSONException
    {
        String requestId = getIdFromJson(rpcResponse);
        if (requests != null && requests.containsKey(requestId))
        {
            Request theRequest = requests.get(requestId);
            requests.remove(requestId);
            Response response = new Response(rpcResponse, theRequest);
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
            Request theRequest = requests.get(requestId);
            requests.remove(requestId);
            theRequest.getResponseBlock().perform(theRequest.createResponse());
        }

        RPCError RPCError = new RPCError(rpcError);
        callback.gatewayReceivedMessage(RPCError);
    }

    // Handlers of outgoing messages

    // Handlers of outgoing messages
    public void makeRequest(Request request) throws JSONException
    {
        if (request.getIdentifier() == null)
        {
            request.setIdentifier(uniqueRequestIdentifier());
        }

        requests.put(request.getIdentifier(), request);
        passMessageToCallback(request.toJsonString());
    }

    private void respondWithResponse(Response response)
    {
        passMessageToCallback(response.toJsonString());
    }

    private void respondWithNotification(Notification notification)
    {
        passMessageToCallback(notification.toJsonString());
    }

    private void respondWithError(RPCError rpcRPCError)
    {
        // format json
        String formatted = rpcRPCError.toJsonString();
        passMessageToCallback(formatted);


    }

    private void passMessageToCallback(String jsonString)
    {
        String escapedJson = StringEscapeUtils.escapeJavaScript(jsonString);
        System.out.println("<------------" + escapedJson);
        callback.gatewayGeneratedMessage(escapedJson);
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
