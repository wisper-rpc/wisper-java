package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.error.WisperException;

/**
 * Created by ehssanhoorvash on 18/02/16.
 */
interface RemoteInstanceCreatorCallback
{
    void result(WisperInstanceModel instanceModel, WisperException ex);
}
