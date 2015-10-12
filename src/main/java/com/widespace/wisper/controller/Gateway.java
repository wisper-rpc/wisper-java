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
 * WebView end point. The controller will handleMessage the incoming objects and parses
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
    private MessageFactory messageFactory;

    public Gateway(GatewayCallback callback)
    {
        this.callback = callback;
        requests = new HashMap<String, Request>();
        extras = new HashMap<String, Object>();
        messageFactory = new MessageFactory();
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

    /**
     * This is a setter for any object that could be contained with the Gateway.
     * This comes handy for example on Android context where we need it.
     *
     * @param key a key assigned to the  resource.
     * @param value the resouce to inject.
     */
    public void setExtra(String key, Object value)
    {
        extras.put(key, value);
    }

    /**
     * Returns the resource specified with this key.
     *
     * @param key key of the resource.
     * @return value of the injected resource.
     */
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
     * handleMessage this request or not, will also handleMessage the request. This is the
     * only method you need to call from the webView delegate to handleMessage RPC
     * Communication.
     *
     * @param message a string representation of the message, the message is
     *                a JSON object and if it is malformed an error will be returned
     *                back to the endpoint in form of an RPCError.
     */
    public void handleMessage(String message)
    {
        try
        {
            handleMessage(new JSONObject(message));
        } catch (JSONException e)
        {
            RPCError err = new RPCErrorBuilder(ErrorDomain.RPC, RPCErrorCodes.PARSE_ERROR.getErrorCode()).withMessage(e.getMessage()).build();
            respondWithError(err);
        }
    }

    /**
     * Handle incoming Wisper message.
     *
     * @param json json object that contains the Wisper message.
     * @throws JSONException
     */
    private void handleMessage(JSONObject json) throws JSONException
    {
        handleMessage(messageFactory.createMessage(json));
    }

    /**
     * Send a message to the other endpoint with a message type
     *
     * @param message the message to be sent to the other endpoint.
     */
    public void handleMessage(AbstractMessage message)
    {
        if (message.type() == RPCMessageType.REQUEST)
        {
            final Request request = (Request) message;
            request.setResponseBlock(new ResponseBlock()
            {
                @Override
                public void perform(Response response, RPCError error)
                {
                    passMessageToCallback(response.toJsonString());
                }
            });
        } else if (message.type() == RPCMessageType.RESPONSE)
        {
            respondBackOnMessage(message);
        } else if (message.type() == RPCMessageType.ERROR)
        {
            respondBackOnMessage(message);

        }

        if (callback != null)
        {
            callback.gatewayReceivedMessage(message);
        }
    }

    private void respondBackOnMessage(AbstractMessage message)
    {
        String identifier = message.getIdentifier();
        if (identifier == null)
            return;

        if (requests.containsKey(identifier))
        {
            Request theRequest = requests.get(identifier);
            requests.remove(identifier);

            if (theRequest.getResponseBlock() != null)
            {
                if (message instanceof RPCError)
                {
                    theRequest.getResponseBlock().perform(null, (RPCError) message);
                } else if (message instanceof Response)
                {
                    theRequest.getResponseBlock().perform((Response) message, null);
                }
            }
        }
    }

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
