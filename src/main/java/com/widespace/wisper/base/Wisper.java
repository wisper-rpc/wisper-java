package com.widespace.wisper.base;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.route.ClassRouter;

/**
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public interface Wisper
{
    void setClassRouter(ClassRouter classRouter);

    //This method is called when this RPC class instance is destructed
    void destruct();

    //This method receives an event name and parameters related to the event
    //void handleStaticEvent(String eventName, Object[] params);
    //void handleEvent(String eventName, Object[] params);

    //Interfaces prior to Java 8 do not permit a static method definition, but this method MUST be implemented by all implementers of this interface
    //public static WisperClassModel registerRpcClass();
}
