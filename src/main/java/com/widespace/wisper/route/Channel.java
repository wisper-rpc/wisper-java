package com.widespace.wisper.route;

import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.AbstractMessage;


public abstract class Channel extends WisperObject implements GatewayCallback
{
    Gateway gateway;

    public static RPCClass registerRpcClass()
    {
        return null;
    }

    public Gateway getGateway()
    {
        return gateway;
    }

    public void setGateway(Gateway gateway)
    {
        this.gateway = gateway;
    }

    @Override
    public void gatewayReceivedMessage(AbstractMessage message)
    {
       // Do Nothing
    }

    @Override
    public void gatewayGeneratedMessage(String message)
    {
       sendMessage(message);
    }

    protected abstract void sendMessage(String message);
    protected abstract void receiveMessage(String message);
}
