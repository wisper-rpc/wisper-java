package com.widespace.wisper.route;


import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.messagetype.error.WisperExceptionHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GatewayRouter extends Router implements GatewayCallback
{
    private Gateway gateway;
    private GatewayCallback gatewayCallback;
    private WisperExceptionHandler exceptionHandler;


    public GatewayRouter()
    {
        this.gateway = new Gateway(this);
        exceptionHandler = new WisperExceptionHandler(gateway);
    }

    public GatewayRouter(@NotNull Gateway gateway)
    {
        this.gateway = gateway;
        this.gateway.setCallback(this);
        exceptionHandler = new WisperExceptionHandler(gateway);
    }

    public GatewayCallback getGatewayCallback()
    {
        return gatewayCallback;
    }

    public void setGatewayCallback(GatewayCallback gatewayCallback)
    {
        this.gatewayCallback = gatewayCallback;
    }

    //=====================================================================================
    //region public Methods
    //=====================================================================================
    public ClassRouter register(Class<? extends Wisper> clazz, String path)
    {
        ClassRouter classRouter = new ClassRouter(clazz);
        exposeRoute(path, classRouter);
        return classRouter;
    }

    //=====================================================================================
    //region Getters Setters
    //=====================================================================================

    public Gateway getGateway()
    {
        return gateway;
    }

    //=====================================================================================
    //region Router overrides
    //=====================================================================================

    @Override
    public void reverseRoute(@NotNull AbstractMessage message, @Nullable String path)
    {
        if (message instanceof Event)
        {
            Event event = (Event) message;
            gateway.sendMessage(new Event(path + event.getMethodName(), event.getParams()));
            return;
        }

        gateway.sendMessage(message);
    }

    //=====================================================================================
    //region Gateway overrides
    //=====================================================================================

    @Override
    public void gatewayReceivedMessage(AbstractMessage message)
    {
        if (gatewayCallback != null)
        {
            gatewayCallback.gatewayReceivedMessage(message);
        }

        String methodName = MessageParser.getFullMethodName(message);

        if (methodName == null || methodName.equals(".handshake"))
        {
            return;
        }

        try
        {
            if (message instanceof CallMessage)
            {
                CallMessage call = (CallMessage) message;
                routeMessage(call, call.getMethodName());
            }
        }
        catch (WisperException wisperException)
        {
            exceptionHandler.handle(wisperException, message);
        }
    }

    @Override
    public void gatewayGeneratedMessage(String message)
    {
        if (gatewayCallback != null)
        {
            gatewayCallback.gatewayGeneratedMessage(message);
        }
        // no-op
    }
}
