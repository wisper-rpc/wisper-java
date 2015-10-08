package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.controller.RPCRemoteObjectController;
import com.widespace.wisper.messagetype.Request;

/**
 * Created by Ehssan Hoorvash on 26/05/14.
 */
public interface CallBlock
{
    void perform(RPCRemoteObjectController remoteObjectController, RPCClassInstance classInstance, RPCClassMethod method, Request request);
}
