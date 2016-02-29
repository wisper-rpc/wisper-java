package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.controller.RemoteObjectController;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.route.Router;

/**
 * Created by Ehssan Hoorvash on 26/05/14.
 */
public interface CallBlock
{
    void perform(RemoteObjectController remoteObjectController, WisperInstanceModel classInstance, WisperMethod method, Request request);

    void perform(Router router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, AbstractMessage message);
}
