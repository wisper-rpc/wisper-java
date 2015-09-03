package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.RPCNotification;
import com.widespace.wisper.messagetype.RPCRequest;
import com.widespace.wisper.messagetype.error.RPCError;

/**
 * Callback interface that is used by the RPCController
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public interface RPCControllerCallback
{
    /**
     * This callback is called when the RPC controller receives a request.
     *
     * @param request the request received by teh rpc controller.
     * @see com.widespace.wisper.messagetype.RPCRequest
     */
    void rpcControllerReceivedRequest(RPCRequest request);


    /**
     * This callback is called when the RPC controller receives a notification.
     *
     * @param notification the notification received by teh rpc controller.
     * @see com.widespace.wisper.messagetype.RPCNotification
     */
    void rpcControllerReceivedNotification(RPCNotification notification);

    /**
     * This callback is called when the RPC controller received an error
     * @param error
     */
    void rpcControllerReceivedError(RPCError error);

    void rpcControllerGeneratedMessage(String escapedJson);
}
