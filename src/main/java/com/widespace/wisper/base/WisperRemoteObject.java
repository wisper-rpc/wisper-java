package com.widespace.wisper.base;

import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.route.EventRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.RemoteObjectEventInterface;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Object intended to be the remote instance representative. You can start calling methods even before the remote
 * is initialized. All messages will be queued up and run sequentially as soon as the remote is ready.
 */
public abstract class WisperRemoteObject implements RemoteObjectEventInterface
{
    public static final ResponseBlock DoNothingResponseBlock = new ResponseBlock()
    {
        @Override
        public void perform(Response response, RPCErrorMessage error)
        {
            // do nothing
        }
    };

    private String mapName;
    private String instanceIdentifier;
    private GatewayRouter gatewayRouter;

    private Queue<IncompleteMessage> messageQueue;
    private EventRouter eventRouter;


    /**
     * Constructs the object with the given map name and gatewayRouter.
     *
     * @param mapName       the map name to be used for this remote object.
     * @param gatewayRouter The gatewayRouter through which the remote object is accessible.
     */

    public WisperRemoteObject(@NotNull String mapName, @NotNull GatewayRouter gatewayRouter)
    {
        this.mapName = mapName;
        this.gatewayRouter = gatewayRouter;
        messageQueue = new LinkedList<IncompleteMessage>();

        exposeRouterOnMapname(mapName, gatewayRouter);
        sendCreateMessageWithParams(null);
    }

    public void sendCreateMessageWithParams(Object[] params)
    {
        if (params == null)
        {
            params = new Object[]{};
        }
        gatewayRouter.getGateway().sendMessage(new Request(mapName + "~", params).withResponseBlock(new ResponseBlock() {
            @Override
            public void perform(Response response, RPCErrorMessage error) {

                String instanceId = (String) (response.getResult() != null ? ((HashMap) response.getResult()).get("id") : null);
                setInstanceIdentifier(instanceId);
            }
        }));
    }

    public void destroy()
    {
        //Send destroy message
        callInstanceMethod("~");

        //Unregister
        unregisterInstanceOnEventRouter(getInstanceIdentifier());
    }

    private void exposeRouterOnMapname(@NotNull String mapName, @NotNull GatewayRouter gatewayRouter)
    {
        if (!gatewayRouter.hasRoute(mapName))
        {
            eventRouter = new EventRouter(this.getClass());
            gatewayRouter.exposeRoute(mapName, eventRouter);
        }
        else
        {
            eventRouter = (EventRouter)gatewayRouter.getRouter(mapName);
        }
    }

    protected void registerInstanceOnEventRouter(String wisperInstanceIdentifier)
    {
        eventRouter.addInstance(wisperInstanceIdentifier, this);
    }

    protected void unregisterInstanceOnEventRouter(String wisperInstanceIdentifier)
    {
        eventRouter.removeInstance(wisperInstanceIdentifier);
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
            registerInstanceOnEventRouter(instanceIdentifier);
            sendEnqueuedMessages();
        }
    }

    private void sendEnqueuedMessages()
    {
        for (IncompleteMessage message : messageQueue)
        {
            gatewayRouter.getGateway().sendMessage(message.completeWithIdentifier(instanceIdentifier));
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
    public void callInstanceMethod(@NotNull String methodName, Object[] params, CompletionBlock completion)
    {
        ResponseBlock block = null;
        if (completion != null)
        {
            block = blockForCompletion(completion);
        }

        if (instanceIdentifier == null)
        {
            messageQueue.add(new IncompleteRequest(mapName + ":" + methodName, params, block));
        } else
        {
            if (completion == null)
            {
                gatewayRouter.getGateway().sendMessage(new Notification(mapName + ":" + methodName, prepend(instanceIdentifier, params)));
            } else
            {
                gatewayRouter.getGateway().sendMessage(new Request(mapName + ":" + methodName, prepend(instanceIdentifier, params)).withResponseBlock(block));
            }
        }
    }

    /**
     * Call a remote instance method with no return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     */
    public void callInstanceMethod(@NotNull String methodName, Object[] params)
    {
        callInstanceMethod(methodName, sanitizeArray(params), null);
    }

    public void callInstanceMethod(@NotNull String methodName)
    {
        callInstanceMethod(methodName, new Object[]{});
    }


    /**
     * Call a remote static method expecting a return value. This message is sent as a request.
     *
     * @param methodName The method name, you should not provide the map name of this class before the method name.
     * @param params     The params you want to pass to the remote method.
     * @param completion Completion block that will be triggered when done with the call.
     */
    public void callStaticMethod(@NotNull String methodName, Object[] params, CompletionBlock completion)
    {
        if (completion == null)
        {
            gatewayRouter.getGateway().sendMessage(new Notification(mapName + "." + methodName, params));
        } else
        {
            gatewayRouter.getGateway().sendMessage(new Request(mapName + "." + methodName, params).withResponseBlock(blockForCompletion(completion)));
        }
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
     * Sends an instance event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendInstanceEvent(@NotNull String name, Object value)
    {
        if (instanceIdentifier == null)
        {
            messageQueue.add(new IncompleteEvent(mapName + ":!", name, value));
        } else
        {
            gatewayRouter.getGateway().sendMessage(new Event(mapName + ":!", instanceIdentifier, name, value));
        }
    }

    /**
     * Sends a static event.
     *
     * @param name  event name.
     * @param value value to be passed along.
     */
    public void sendStaticEvent(@NotNull String name, Object value)
    {
        gatewayRouter.getGateway().sendMessage(new Event(mapName + "!", name, value));
    }

    public static void handleStaticEvent(Event event)
    {
        // NO-USE
    }

    @Override
    abstract public void handleInstanceEvent(Event event);



    //region Privates
    //=============================================

    interface IncompleteMessage
    {
        AbstractMessage completeWithIdentifier(String id);
    }

    class IncompleteEvent implements IncompleteMessage
    {
        private final String method;
        private final String name;
        private final Object value;

        public IncompleteEvent(String method, String name, Object value)
        {
            this.method = method;
            this.name = name;
            this.value = value;
        }

        public AbstractMessage completeWithIdentifier(String id)
        {
            return new Event(method, id, name, value);
        }
    }

    class IncompleteRequest implements IncompleteMessage
    {
        private final String method;
        private final Object[] params;
        private final ResponseBlock block;

        public IncompleteRequest(String method, Object[] params, ResponseBlock block)
        {
            this.method = method;
            this.params = params;
            this.block = block;
        }

        public AbstractMessage completeWithIdentifier(String id)
        {
            if (block == null)
            {
                return new Notification(method, prepend(id, params));
            }

            return new Request(method, prepend(id, params)).withResponseBlock(block);
        }
    }

    private static Object[] prepend(Object param, Object[] initialParams)
    {
        // Must create a copy of the parameters that's one longer
        Object[] params = new Object[initialParams.length + 1];
        System.arraycopy(initialParams, 0, params, 1, initialParams.length);

        // And add the id
        params[0] = param;

        return params;
    }

    private static Object[] sanitizeArray(Object[] initialParams)
    {
        List<Object> objList = new ArrayList<Object>();
        for (Object element : initialParams)
        {
            if(element != null)
            {
                objList.add(element);
            }
        }

        return objList.toArray();
    }

    @NotNull
    private ResponseBlock blockForCompletion(@Nullable final CompletionBlock completion)
    {
        if (completion == null)
        {
            return DoNothingResponseBlock;
        }

        return new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                completion.perform(response.getResult(), error);
            }
        };
    }
}
