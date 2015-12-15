package com.widespace.wisper.controller;


import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.*;
import com.widespace.wisper.messagetype.error.Error;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Gateway is the receiving end point for RPC messages coming from a
 * WebView end point. The controller will handleMessage the incoming objects and parses
 * them into a model objects that are easier to interact with.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */

public class Gateway
{
    protected GatewayCallback callback;
    private HashMap<String, Request> requests;

    private HashMap<String, Object> extras;
    private MessageFactory messageFactory;

    private static int requestCount = 0;

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
        requestCount++;
        return "WISPER-ANDROID-" + String.valueOf(requestCount);
    }

    /**
     * This is a setter for any object that could be contained with the Gateway.
     * This comes handy for example on Android context where we need it.
     *
     * @param key   a key assigned to the  resource.
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


    /**
     * Returns all the extras currently registered with this Gateway.
     *
     * @return a HashMap of the extras containing a String as key and an Object as the value of the extra.
     */
    public HashMap getExtras()
    {
        return extras;
    }


    /**
     * Entry point for the RPC interface from the WebView. Decides if we can
     * handleMessage this request or not, will also handleMessage the request. This is the
     * only method you need to call from the webView delegate to handleMessage RPC
     * Communication.
     * <p/>
     * If the message is not meaningful to this gateway, a Wisper Error will be sent back.
     *
     * @param message a string representation of the message, the message is
     *                a JSON object and if it is malformed an error will be returned
     *                back to the endpoint in form of an RPCErrorMessage.
     */
    public void handleMessage(String message)
    {
        System.out.println(" JS ----> N :  " + message);
        try
        {
            handleMessage(new JSONObject(message));
        } catch (JSONException e)
        {
            RPCErrorMessage errorMessage = new RPCErrorMessageBuilder(ErrorDomain.NATIVE, Error.PARSE_ERROR.getCode())
                    .withMessage("Message could not be prased as a valid JSON message.")
                    .withName(Error.PARSE_ERROR.getDescription())
                    .build();
            sendMessage(errorMessage);
        }
    }

    /**
     * Handle incoming Wisper message.
     *
     * @param json json object that contains the Wisper message.
     * @throws JSONException
     */
    private void handleMessage(JSONObject json)
    {
        AbstractMessage message = messageFactory.createMessage(json);
        if (message == null)
        {
            RPCErrorMessage errorMessage = new RPCErrorMessageBuilder(ErrorDomain.NATIVE, Error.PARSE_ERROR.getCode())
                    .withMessage("The message could not be parsed as a valid RPC message. Invalid Json or Wrong format? ")
                    .withName(Error.FORMAT_ERROR.getDescription())
                    .build();
            sendMessage(errorMessage);
            return;
        }

        handleMessage(message);
    }

    /**
     * Handles incoming Wisper message.
     *
     * @param message the message.
     */
    public void handleMessage(AbstractMessage message)
    {
        if (message.type() == RPCMessageType.REQUEST)
        {
            final Request request = (Request) message;
            request.setResponseBlock(new ResponseBlock()
            {
                @Override
                public void perform(Response response, RPCErrorMessage error)
                {
                    if (response != null)
                        sendMessage(response);
                    else
                        sendMessage(error);

                    request.setResponseBlock(new ResponseBlock()
                    {
                        @Override
                        public void perform(Response response, RPCErrorMessage error)
                        {
                            //NO-OP
                        }
                    });
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
        {
            return;
        }

        if (requests.containsKey(identifier))
        {
            Request theRequest = requests.get(identifier);
            requests.remove(identifier);

            if (theRequest.getResponseBlock() != null)
            {
                if (message instanceof RPCErrorMessage)
                {
                    theRequest.getResponseBlock().perform(null, (RPCErrorMessage) message);
                } else if (message instanceof Response)
                {
                    theRequest.getResponseBlock().perform((Response) message, null);
                }
            }
        }
    }

    /**
     * Sends the message to the the callback. If the message is a Request,
     * a unique identifier per session is appended to the message.
     *
     * @param message the Wisper message to be sent.
     * @see Request
     * @see AbstractMessage
     */
    public void sendMessage(AbstractMessage message)
    {
        if (message.type() == RPCMessageType.REQUEST)
        {
            String identifier = uniqueRequestIdentifier();
            ((Request) message).setIdentifier(identifier);

            requests.put(identifier, (Request) message);
        }

        sendMessage(message.toJsonString());
    }

    /**
     * Sends the message to the the callback directly. The message must be meaningful to the other endpoint.
     *
     * @param message The message.
     */
    public void sendMessage(String message)
    {
        callback.gatewayGeneratedMessage(message);
    }

    /**
     * The WisperCallback that receives the generated messages from this Gateway and knows
     * when a message has been received by this endpoint.
     *
     * @return The callback.
     */
    public GatewayCallback getCallback()
    {
        return callback;
    }

    /**
     * Replaces the callback. The previous callback will no longer receive calls.
     *
     * @param callback The new callback.
     */
    public void setCallback(GatewayCallback callback)
    {
        this.callback = callback;
    }
}
