package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.route.ClassRouter;


public interface CallBlock
{
    void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, AbstractMessage message) throws Exception;
}
