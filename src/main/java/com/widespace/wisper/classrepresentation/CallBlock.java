package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.route.ClassRouter;


public interface CallBlock
{
    void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, CallMessage message) throws Exception;
}
