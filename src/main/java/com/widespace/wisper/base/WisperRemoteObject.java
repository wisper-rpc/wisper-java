package com.widespace.wisper.base;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Object intended to be the remote instance representative. You can start calling methods even before the remote
 * is initialized. All messages will be queued up and run sequentially as soon as the remote is ready.
 */
public class WisperRemoteObject
{
    private String mapName;
    private String instanceIdentifier;
    private Gateway gateway;

    private Queue<IncompleteMessage> messageQueue;


    /**
     * Constructs the object with the given map name and gateway.
     *
     * @param mapName the map name to be used for this remote object.
     * @param gateway The gateway through which the remote object is accessible.
     */

    public WisperRemoteObject(@NotNull String mapName, @NotNull Gateway gateway)
    {
        this.mapName = mapName;
        this.gateway = gateway;
        messageQueue = new LinkedList<IncompleteMessage>();
    }

    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    public void setInstanceIdentifier(String instanceIdentifier)
    {
        if (instanceIdentifier != null)
        {
            this.instanceIdentifier = instanceIdentifier;
            sendEnqueuedMessages();
        }
    }

    private void sendEnqueuedMessages()
    {
        for (IncompleteMessage message : messageQueue)
        {
            gateway.sendMessage(message.completeWithIdentifier(instanceIdentifier));
        }

        // Let the GC reclaim the queue.
        messageQueue = null;
    }

    /**
     * Call a remote instance method expecting a return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     * @param completion Completion block that will be triggered when done with the call.
     */

    public void callInstanceMethod(@NotNull String methodName, Object[] params, final CompletionBlock completion)
    {
        ResponseBlock block = new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                if (completion != null)
                {
                    completion.perform(response.getResult(), error);
                }
            }
        };

        deferAndSendInstanceCalls(new Request(mapName + ":" + methodName, params).withResponseBlock(block));

    }

    /**
     * Call a remote instance method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     */
    public void callInstanceMethod(@NotNull String methodName, Object... params)
    {
        callInstanceMethod(methodName, params, null);
    }

    /**
     * Call a remote static method expecting a return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     * @param completion Completion block that will be triggered when done with the call.
     */
    public void callStaticMethod(@NotNull String methodName, Object[] params, final CompletionBlock completion)
    {
        ResponseBlock block = new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                if (completion != null)
                {
                    completion.perform(response.getResult(), error);
                }
            }
        };

        gateway.sendMessage(new Request(mapName + "." + methodName, params).withResponseBlock(block));
    }


    /**
     * Call a remote static method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The parameters array you want to pass to the remote method.
     */
    public void callStaticMethod(@NotNull String methodName, Object... params)
    {
        callStaticMethod(methodName, params, null);
    }

    /**
     * Sends an instance event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendInstanceEvent(@NotNull String name, Object value)
    {
        deferAndSendInstanceCalls(new Notification(mapName + ":!", name, value));
    }

    /**
     * Sends a static event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendStaticEvent(@NotNull String name, Object value)
    {
        gateway.sendMessage(new Notification(mapName + "!", name, value));
    }

    /**
     * holds the messaged in a queue until an instance identifier is received.
     *
     * @param message
     */
    private void deferAndSendInstanceCalls(AbstractMessage message)
    {
        if (instanceIdentifier != null)
        {
            gateway.sendMessage(message);
            return;
        }

        messageQueue.add(new IncompleteMessage(message));
    }

    class IncompleteMessage
    {
        private final AbstractMessage message;

        public IncompleteMessage(AbstractMessage message)
        {
            this.message = message;
        }

        public AbstractMessage completeWithIdentifier(String id)
        {
            if (message instanceof Request)
            {
                return request((Request) message, id);
            }
            if (message instanceof Event)
            {
                return event((Event) message, id);
            }

            return message;
        }

        private AbstractMessage event(Event event, String id)
        {
            return new Event(event.getMethodName(), id, event.getParams());
        }

        private AbstractMessage request(Request request, String id)
        {
            return new Request(request.getMethodName(), id, request.getParams()).withResponseBlock(request.getResponseBlock());
        }
    }
}
