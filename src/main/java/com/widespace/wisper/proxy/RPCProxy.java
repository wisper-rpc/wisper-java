package com.widespace.wisper.proxy;

import com.widespace.wisper.controller.RPCController;
import com.widespace.wisper.messagetype.RPCNotification;
import com.widespace.wisper.messagetype.RPCRequest;
import com.widespace.wisper.utils.StringUtils;
import org.json.JSONException;

/**
 * Acts as an adapter between two remote object controllers.
 */
public class RPCProxy
{
    /**
     * The object to forward the RPC call to.
     * Do not retain this object!
     */
    private RPCController receiver;
    /**
     * The name of the resource we want to proxy that is available in the receiver.
     */
    private String receiverMapName;
    /**
     * The name that this proxy listens to when registerer with an RPCController.
     */
    private String mapName;


    /**
     * Takes a request, transforms it and passes it on to the receiver.
     *
     * @param request The request that is trying to reach the other controller
     * @see com.widespace.wisper.messagetype.RPCRequest
     */
    public void handleRequest(RPCRequest request) throws JSONException
    {
        RPCRequest proxifiedRequest = new RPCRequest();
        proxifiedRequest.setIdentifier(request.getIdentifier());
        proxifiedRequest.setMethodName(extractMethodName(request.getMethodName()));
        proxifiedRequest.setParams(request.getParams());
        proxifiedRequest.setResponseBlock(request.getResponseBlock());
        receiver.handle(proxifiedRequest.toJsonString());
    }

    /**
     * Takes a notification, transforms it and passes it on to the receiver.
     *
     * @param notification The notification that is trying to reach the other controller
     * @see com.widespace.wisper.messagetype.RPCNotification
     */
    public void handleNotification(RPCNotification notification) throws JSONException
    {
        RPCNotification proxifiedNotification = new RPCNotification();
        proxifiedNotification.setMethodName(extractMethodName(notification.getMethodName()));
        proxifiedNotification.setParams(notification.getParams());
        receiver.handle(proxifiedNotification.toJsonString());
    }



    private String extractMethodName(String methodName)
    {
        int indexOfProxyName = methodName.indexOf(this.mapName);
        if (indexOfProxyName != StringUtils.NOT_FOUND_INDEX)
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.receiverMapName);
            builder.append(methodName.substring(this.getMapName().length()));
            return builder.toString();
        }

        return null;
    }

    public RPCController getReceiver()
    {
        return receiver;
    }

    public void setReceiver(RPCController receiver)
    {
        this.receiver = receiver;
    }

    public String getReceiverMapName()
    {
        return receiverMapName;
    }

    public void setReceiverMapName(String receiverMapName)
    {
        this.receiverMapName = receiverMapName;
    }

    public String getMapName()
    {
        return mapName;
    }

    public void setMapName(String mapName)
    {
        this.mapName = mapName;
    }
}