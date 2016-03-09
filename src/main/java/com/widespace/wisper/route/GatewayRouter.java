package com.widespace.wisper.route;


import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GatewayRouter extends Router implements GatewayCallback
{
    private Gateway gateway;

    public GatewayRouter(Gateway gateway)
    {
        this.gateway = gateway;
        this.gateway.setCallback(this);
    }

    @Override
    public void gatewayReceivedMessage(AbstractMessage message)
    {
        if (message instanceof Notification)
            routeMessage(message, ((Notification) message).getMethodName());

        else if (message instanceof Request)
            routeMessage(message, ((Request) message).getMethodName());
    }


    @Override
    public void reverseRoute(@NotNull AbstractMessage message, @Nullable String path)
    {
        if(message instanceof Event)
        {
            String methodName = ((Event) message).getMethodName();
            ((Event) message).setMethodName(path+methodName);
        }

        gateway.sendMessage(message);
    }

    @Override
    public void gatewayGeneratedMessage(String message)
    {
        // no-op
    }


    public void register(String path, Class<? extends Wisper> clazz)
    {
        ClassRouter classRouter = new ClassRouter(clazz);
        exposeRoute(path, classRouter);
    }

}
