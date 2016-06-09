package com.widespace.wisper.route;

import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.AbstractMessage;


public abstract class Channel extends WisperObject implements GatewayCallback
{
    Gateway gateway;

    public static WisperClassModel registerRpcClass()
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

    //Actions
    public abstract void sendMessage(String message);

    public void receiveMessage(String message)
    {
        if (gateway != null)
        {
            gateway.handleMessage(message);
        }
    }


    // Gateway callbacks
    @Override
    public void gatewayReceivedMessage(AbstractMessage message)
    {
        //no-op
    }

    @Override
    public void gatewayGeneratedMessage(String message)
    {
        sendMessage(message);
    }
}
