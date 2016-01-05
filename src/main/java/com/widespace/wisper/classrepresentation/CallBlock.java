package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.controller.RemoteObjectController;
import com.widespace.wisper.messagetype.Request;

/**
 * Created by Ehssan Hoorvash on 26/05/14.
 */
public interface CallBlock
{
    void perform(RemoteObjectController remoteObjectController, WisperClassInstance classInstance, WisperMethod method, Request request);
}
