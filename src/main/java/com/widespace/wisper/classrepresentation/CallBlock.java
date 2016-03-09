package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.route.Router;

/**
 * Created by Ehssan Hoorvash on 26/05/14.
 */
public interface CallBlock
{
    void perform(Router router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, AbstractMessage message);
}
