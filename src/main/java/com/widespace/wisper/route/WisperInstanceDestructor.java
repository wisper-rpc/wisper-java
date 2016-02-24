package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;


public class WisperInstanceDestructor
{

    private Request message;

    public WisperInstanceDestructor(@NotNull AbstractMessage message) throws WisperException
    {
        if (MessageParser.getCallType(message) != WisperCallType.DESTROY_INSTANCE)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance creator was called with a non-DESTROY_INSTANCE message type.");

        this.message = (Request) message;
    }

    public void destroy() throws WisperException
    {
        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        destroy(instanceIdentifier);
    }

    public void destroy(String wisperInstanceIdentifier) throws WisperException
    {
        WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId(wisperInstanceIdentifier);
        if (instanceModel == null)
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this router under instance ID :" + wisperInstanceIdentifier);

        instanceModel.getInstance().destruct();

        respondToDestructRequest(wisperInstanceIdentifier);

        WisperInstanceRegistry.sharedInstance().removeInstance(wisperInstanceIdentifier);
    }

    private void respondToDestructRequest(String wisperInstanceIdentifier)
    {
        Response response = message.createResponse();
        response.setResult(wisperInstanceIdentifier);
        if (message.getResponseBlock() != null)
        {
            message.getResponseBlock().perform(response, null);
        }
    }

}


