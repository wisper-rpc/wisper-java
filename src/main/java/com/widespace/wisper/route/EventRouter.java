package com.widespace.wisper.route;

import com.widespace.wisper.base.WisperRemoteObject;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;

import java.rmi.server.RemoteObject;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by ehssanhoorvash on 09/05/16.
 */
public class EventRouter extends Router
{
    private Map<String, RemoteObjectEventInterface> remoteObjects;

    public EventRouter(RemoteObjectEventInterface wisperRemoteObject)
    {
        remoteObjects = new HashMap<String, RemoteObjectEventInterface>();
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
                    super.routeMessage(message, path);
            }
            return;
        }

        super.routeMessage(message, path);
    }

    public Map<String, RemoteObjectEventInterface> getRemoteObjects()
    {
        return remoteObjects;
    }

    private void handleStaticEvent(Notification message)
    {
        for (Map.Entry<String, RemoteObjectEventInterface> entry : remoteObjects.entrySet())
        {
            RemoteObjectEventInterface remoteObject = (RemoteObjectEventInterface) entry.getValue();
            remoteObject.handleStaticEvent(new Event(message));
        }
    }

    private void handleInstanceEvent(Notification message)
    {
        Event instanceEvent = new Event(message);

        if (remoteObjects.containsKey(instanceEvent.getInstanceIdentifier()))
            remoteObjects.get(instanceEvent.getInstanceIdentifier()).handleInstanceEvent(instanceEvent);
    }

    public void addInstance(String instanceIdentifier, RemoteObjectEventInterface wisperRemoteObject)
    {
        remoteObjects.put(instanceIdentifier, wisperRemoteObject);
    }

    public void removeInstance(String instanceIdentifier)
    {
        if (remoteObjects.containsKey(instanceIdentifier))
            remoteObjects.remove(instanceIdentifier);
    }

}
