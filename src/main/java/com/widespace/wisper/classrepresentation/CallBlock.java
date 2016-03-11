package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.route.ClassRouter;


public interface CallBlock
{
    void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, Request request) throws Exception;
}
