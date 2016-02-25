package com.widespace.wisper.base;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Object intended to be the remote instance representative. You can start calling methods even before the remote
 * is initialized. All messages will be queued up and run sequentially as soon as the remote is ready.
 */
public class WisperRemoteObject
{
    private String mapName;
    private String instanceIdentifier;
    private Gateway gateway;

    private MessageQueue<AbstractMessage> instanceMessageQueue;


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
        instanceMessageQueue = new MessageQueue<AbstractMessage>();
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
        while (instanceMessageQueue.hasMessage())
        {
            AbstractMessage message = instanceMessageQueue.pop();

            if (message instanceof Request)
            {
                Object[] objects = ((Request) message).getParams() != null ? ((Request) message).getParams() : new Object[]{};
                List<Object> newParams = new LinkedList<Object>(Arrays.asList(objects));
                newParams.add(0, instanceIdentifier);
                ((Request) message).setParams(newParams.toArray(new Object[newParams.size()]));
            }
            if (message instanceof Event)
            {
                Object[] objects = ((Event) message).getParams() != null ? ((Event) message).getParams() : new Object[]{};
                List<Object> newParams = new LinkedList<Object>(Arrays.asList(objects));
                newParams.add(0, instanceIdentifier);
                ((Event) message).setParams(newParams.toArray(new Object[newParams.size()]));

            }
            gateway.sendMessage(message);

        }
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
        final Request request = new Request();
        request.setMethod(mapName + ":" + methodName);
        request.setParams(params);
        request.setResponseBlock(new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                if (completion != null)
                    completion.perform(response.getResult(), error);
            }
        });
        deferAndSendInstanceCalls(request);

    }

    /**
     * Call a remote instance method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     */
    public void callInstanceMethod(@NotNull String methodName, Object[] params)
    {
        callInstanceMethod(methodName, params, null);
    }

    public void callInstanceMethod(@NotNull String methodName, Object params)
    {
        callInstanceMethod(methodName, params!= null ? new Object[]{params} : null, null);
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
        final Request request = new Request();
        request.setMethod(mapName + "." + methodName);
        request.setParams(params);
        request.setResponseBlock(new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                if (completion != null)
                    completion.perform(response.getResult(), error);
            }
        });

        gateway.sendMessage(request);
    }


    /**
     * Call a remote static method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The parameters array you want to pass to the remote method.
     */
    public void callStaticMethod(@NotNull String methodName, Object[] params)
    {
        callStaticMethod(methodName, params, null);
    }


    /**
     * Call a remote static method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param param      The parameter you want to pass to the remote method.
     */
    public void callStaticMethod(@NotNull String methodName, Object param)
    {
        callStaticMethod(methodName, new Object[]{param}, null);
    }


    /**
     * Sends an instance event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendInstanceEvent(@NotNull String name, Object value)
    {
        Notification notification = new Notification();
        notification.setMethodName(mapName + ":!");
        notification.setParams(new Object[]{name, value});
        deferAndSendInstanceCalls(notification);

    }

    /**
     * Sends a static event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendStaticEvent(@NotNull String name, Object value)
    {
        Notification notification = new Notification();
        notification.setMethodName(mapName + "!");
        notification.setParams(new Object[]{name, value});
        gateway.sendMessage(notification);
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

        instanceMessageQueue.push(message);
    }
}
