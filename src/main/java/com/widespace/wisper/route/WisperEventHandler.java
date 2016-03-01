package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;

public class WisperEventHandler
{
    private Router router;
    private WisperClassModel wisperClassModel;
    private AbstractMessage message;

    public WisperEventHandler(@NotNull Router router, @NotNull WisperClassModel wisperClassModel, @NotNull AbstractMessage message)
    {
        if (! (message instanceof Notification))
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance event handler was called with a non-Notification message type.");

        WisperCallType callType = MessageParser.getCallType(message);
        if (callType != WisperCallType.STATIC_EVENT && callType != WisperCallType.INSTANCE_EVENT)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance event handler was called with a non-Event message call type.");

        this.router = router;
        this.wisperClassModel = wisperClassModel;
        this.message = message;
    }

    public void handle()
    {


    }
}
