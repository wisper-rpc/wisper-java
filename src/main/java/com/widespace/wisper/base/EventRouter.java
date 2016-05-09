package com.widespace.wisper.base;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.route.FunctionRouter;
import com.widespace.wisper.route.MessageParser;
import com.widespace.wisper.route.WisperCallType;

import java.util.HashMap;
import java.util.Map;

import sun.misc.MessageUtils;

/**
 * Created by ehssanhoorvash on 09/05/16.
 */
public class EventRouter extends FunctionRouter
{
    private WisperRemoteObject wisperRemoteObject;
    private Map<String, WisperRemoteObject> remoteObjects;

    public EventRouter(WisperRemoteObject wisperRemoteObject)
    {
        this.wisperRemoteObject = wisperRemoteObject;
        remoteObjects = new HashMap<String, WisperRemoteObject>();
    }

    @Override
    public void routeMessage(AbstractMessage message, String path) throws WisperException
    {
        if (message instanceof Notification)
        {
            WisperCallType callType = MessageParser.getCallType(message);
            switch (callType)
            {
                case STATIC_EVENT:
                {
                    handleStaticEvent((Notification) message);
                }
                break;
                case INSTANCE_EVENT:
                {
                    handleInstanceEvent((Notification) message);
                }
                break;
                default:
                    //todo: err
                    break;
            }
        }
    }

    public Map<String, WisperRemoteObject> getRemoteObjects()
    {
        return remoteObjects;
    }

    private void handleStaticEvent(Notification message)
    {
        wisperRemoteObject.handleStaticEvent(new Event(message));
    }

    private void handleInstanceEvent(Notification message)
    {
        Event instanceEvent = new Event(message);
    }

    public void addInstance(String instanceIdentifier, WisperRemoteObject wisperRemoteObject)
    {
        remoteObjects.put(instanceIdentifier, wisperRemoteObject);
    }

    public void removeInstance(String instanceIdentifier)
    {
        if (remoteObjects.containsKey(instanceIdentifier))
            remoteObjects.remove(instanceIdentifier);
    }

}
