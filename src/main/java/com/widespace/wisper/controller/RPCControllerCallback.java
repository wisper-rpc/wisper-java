package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.*;


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
     * @see com.widespace.wisper.messagetype.Request
     */
    void rpcControllerReceivedRequest(Request request);


    /**
     * This callback is called when the RPC controller receives a notification.
     *
     * @param notification the notification received by teh rpc controller.
     * @see com.widespace.wisper.messagetype.Notification
     */
    void rpcControllerReceivedNotification(Notification notification);

    /**
     * This callback is called when the RPC controller received an RPCError
     * @param RPCError
     */
    void rpcControllerReceivedError(RPCError RPCError);

    void rpcControllerGeneratedMessage(String escapedJson);
}
