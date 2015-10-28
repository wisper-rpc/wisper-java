package com.widespace.wisper.base;

import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.controller.RemoteObjectController;

/**
 * This class could be extended by all the classes that require registration to the RPC
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public abstract class WisperObject implements Wisper
{
    protected RemoteObjectController remoteObjectController;

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
    public void setRemoteObjectController(RemoteObjectController remoteObjectController)
    {
        this.remoteObjectController = remoteObjectController;
    }

    @Override
    public void destruct()
    {
        this.remoteObjectController = null;
    }
}