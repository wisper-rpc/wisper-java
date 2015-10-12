package com.widespace.wisper.base;

import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.classrepresentation.RPCClassInstance;
import com.widespace.wisper.controller.RPCRemoteObjectController;
import com.widespace.wisper.messagetype.Notification;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class could be extended by all the classes that require registration to the RPC
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public abstract class RPCObject implements RPCProtocol
{
    protected RPCRemoteObjectController remoteObjectController;
    private RPCClassInstance rpcClassInstance;

    /**
     * This method must be implemented by any object desiring to register itself as RPC
     * unfortunately prior to Java 8 we cannot have static methods in Interfaces which is why we need to take this approach instead.
     *
     * @return RPCClass an instance of the RPC class model object containing the models of methods
     */
    public static RPCClass registerRpcClass()
    {
        return null;
    }

    @Override
    public void setRemoteObjectController(RPCRemoteObjectController remoteObjectController)
    {

        this.remoteObjectController = remoteObjectController;
    }

    @Override
    public void destruct()
    {
        this.remoteObjectController = null;
    }

    /**
     * Notify the other endpoint about something.
     *
     * @param notification The notification carrying the event we want to send. This method will add the instance ID as the first param if the method contains ":!". You only need to have set the method correctly for this to work.
     */
    public void sendNotification(Notification notification)
    {
        if (notification != null)
        {
            if (notification.getMethodName().contains(":!"))
            {
                ArrayList<Object> params = new ArrayList<Object>();
                params.add(rpcClassInstance.getInstanceIdentifier());
                params.addAll(Arrays.asList(notification.getParams()));
                notification.setParams(params);
            }

            remoteObjectController.handleMessage(notification);
        }
    }

    /**
     * Create a notification already prefilled with the correct method for this class and instance event.
     * If remoteObjectController is not set this method will return nil.
     *
     * @throws JSONException
     * @see com.widespace.wisper.base.RPCProtocol
     */
    public Notification createEventNotification(ArrayList<Object> params) throws JSONException
    {
        if (rpcClassInstance == null)
        {
            return null;
        }

        Notification notification = new Notification();
        notification.setParams(params);
        notification.setMethodName(remoteObjectController.getRpcClassForClass(this.getClass()).getMapName() + ":!");
        return notification;
    }

    /**
     * Create a notification already prefilled with the correct method for this class and instance event.
     * If remoteObjectController is not set this method will return nil.
     *
     * @throws JSONException
     * @see com.widespace.wisper.base.RPCProtocol
     */
    public Notification createClassEventNotification(ArrayList<Object> params) throws JSONException
    {
        if (rpcClassInstance == null)
        {
            return null;
        }

        Notification notification = new Notification();
        notification.setParams(params);
        notification.setMethodName(remoteObjectController.getRpcClassForClass(this.getClass()).getMapName() + "!");
        return notification;
    }


}
