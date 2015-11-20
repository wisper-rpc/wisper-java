package com.widespace.wisper.messagetype;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
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
                return new RPCErrorMessage(json);
            case UNKNOWN:
            default:
                return null;
        }
    }

    // Utility
    public RPCMessageType determineMessageType(JSONObject jsonMessage)
    {
        RPCMessageType result = RPCMessageType.UNKNOWN;
        if (jsonMessage == null)
        {
            return RPCMessageType.UNKNOWN;
        }

        if (jsonMessage.has(Constants.METHOD) && jsonMessage.has(Constants.PARAMS))
        {
            if (jsonMessage.has(Constants.ID))
            {
                result = RPCMessageType.REQUEST;
            } else
            {
                result = RPCMessageType.NOTIFICATION;
            }
        } else if (jsonMessage.has(Constants.RESULT) && jsonMessage.has(Constants.ID))
        {
            result = RPCMessageType.RESPONSE;
        } else if (jsonMessage.has(Constants.ERROR))
        {
            result = RPCMessageType.ERROR;
        }

        return result;
    }

    public RPCMessageType determineMessageType(AbstractMessage message)
    {
        if (message == null)
        {
            return RPCMessageType.UNKNOWN;
        }

        return message.type();
    }
}
