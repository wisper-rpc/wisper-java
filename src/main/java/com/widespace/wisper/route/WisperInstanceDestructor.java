package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;


public class WisperInstanceDestructor
{

    public WisperInstanceDestructor(AbstractMessage message) throws WisperException
    {
        if (MessageParser.getCallType(message) != WisperCallType.DESTROY_INSTANCE)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance creator was called with a non-DESTROY_INSTANCE message type.");
    }

    public void destroy(String wisperInstanceIdentifier)
    {
        WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId(wisperInstanceIdentifier);
        if (instanceModel == null)
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this router under instance ID :" + wisperInstanceIdentifier);

        instanceModel.getInstance().destruct();
        WisperInstanceRegistry.sharedInstance().removeInstance(wisperInstanceIdentifier);
    }
}
