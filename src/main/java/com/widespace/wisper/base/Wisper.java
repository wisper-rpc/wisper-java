package com.widespace.wisper.base;

import com.widespace.wisper.controller.RPCRemoteObjectController;

/**
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public interface Wisper
{
    //gets the remote object controller for this RPC Class instance in case of creation
    void setRemoteObjectController(RPCRemoteObjectController remoteObjectController);

    //This method is called when this RPC class instance is destructed
    void destruct();

    //This method receives an event name and parameters related to the event
    //void handleStaticEvent(String eventName, Object[] params);
    //void handleEvent(String eventName, Object[] params);

    //Interfaces prior to Java 8 do not permit a static method definition, but this method MUST be implemented by all implementers of this interface
    //public static RPCClass registerRpcClass();
}
