package com.widespace.wisper.proxy;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.utils.StringUtils;

/**
 * Acts as an adapter between two remote object controllers.
 */
public class RPCProxy
{
    /**
     * The object to forward the RPC call to.
     * Do not retain this object!
     */
    private Gateway receiver;
    /**
     * The name of the resource we want to proxy that is available in the receiver.
     */
    private String receiverMapName;
    /**
     * The name that this proxy listens to when registerer with an Gateway.
     */
    private String mapName;


    /**
     * Takes a request, transforms it and passes it on to the receiver.
     *
     * @param request The request that is trying to reach the other controller
     * @see Request
     */
    public void handleRequest(Request request)
    {
        Request proxied = new Request(extractMethodName(request.getMethodName()), request.getResponseBlock(), request.getParams());
        proxied.setIdentifier(request.getIdentifier());
        receiver.handleMessage(proxied.toJsonString());
    }

    /**
     * Takes a notification, transforms it and passes it on to the receiver.
     *
     * @param notification The notification that is trying to reach the other controller
     * @see Notification
     */
    public void handleNotification(Notification notification)
    {
        receiver.handleMessage(new Notification(extractMethodName(notification.getMethodName()), notification.getParams()).toJsonString());
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

    public Gateway getReceiver()
    {
        return receiver;
    }

    public void setReceiver(Gateway receiver)
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
