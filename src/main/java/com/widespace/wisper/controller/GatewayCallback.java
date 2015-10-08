package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.AbstractMessage;


/**
 * Callback interface that is used by the RPCController
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public interface GatewayCallback
{

    /**
     * An incoming message was parsed
     *
     * @param message Wisper message representation of the parsed message.
     */
    void gatewayReceivedMessage(AbstractMessage message);

    /**
     * Generated outgoing message from whatever request/response/notification you ran through this gateway.
     *
     * @param message The message as a JSON string to be sent to some other Wisper Gateway.
     */
    void gatewayGeneratedMessage(String message);
}
