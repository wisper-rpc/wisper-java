package com.widespace.wisper.messagetype;

import com.widespace.wisper.messagetype.error.RPCError;
import org.json.JSONObject;

public class MessageFactory
{

    public AbstractMessage createMessage(JSONObject json)
    {
        RPCMessageType type = determineMessageType(json);
        switch (type)
        {
            case REQUEST:
                return new Request(json);
            case RESPONSE:
                return new Response(json);
            case NOTIFICATION:
                return new Notification(json);
            case ERROR:
                return new RPCError(json);
            case UNKNOWN:
            default:
                return null;
        }
    }

    // Utility
    public RPCMessageType determineMessageType(JSONObject jsonMessage)
    {
        RPCMessageType result = RPCMessageType.UNKNOWN;
        if(jsonMessage == null)
        {
            return RPCMessageType.UNKNOWN;
        }

        if (jsonMessage.has("method") && jsonMessage.has("params"))
        {
            if (jsonMessage.has("id"))
            {
                result = RPCMessageType.REQUEST;
            } else
            {
                result = RPCMessageType.NOTIFICATION;
            }
        } else if (jsonMessage.has("result") && jsonMessage.has("id"))
        {
            result = RPCMessageType.RESPONSE;
        } else if (jsonMessage.has("error"))
        {
            result = RPCMessageType.ERROR;
        }

        return result;
    }
}
