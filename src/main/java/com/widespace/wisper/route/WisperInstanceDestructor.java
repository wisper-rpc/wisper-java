package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;


public class WisperInstanceDestructor
{

    private final Router router;
    private Request message;

    public WisperInstanceDestructor(@NotNull AbstractMessage message, @NotNull Router router) throws WisperException
    {
        if (MessageParser.getCallType(message) != WisperCallType.DESTROY_INSTANCE)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance creator was called with a non-DESTROY_INSTANCE message type.");

        this.message = (Request) message;
        this.router = router;
    }

    public WisperInstanceDestructor(@NotNull Router router)
    {
        this.router = router;
    }

    public void destroy() throws WisperException
    {
        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        destroy(instanceIdentifier);
    }

    public void destroy(String wisperInstanceIdentifier) throws WisperException
    {
        WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceUnderRoute(wisperInstanceIdentifier, router.getRootRoute());
        if (instanceModel == null)
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this router under instance ID :" + wisperInstanceIdentifier);

        instanceModel.getInstance().destruct();

        sendDestroyEvent(instanceModel);

        respondToDestructRequest(wisperInstanceIdentifier);

        WisperInstanceRegistry.sharedInstance().removeInstance(wisperInstanceIdentifier);
    }

    private void sendDestroyEvent(WisperInstanceModel instanceModel)
    {
        Event destroyEvent = new WisperEventBuilder().withInstanceIdentifier(instanceModel.getInstanceIdentifier()).withMethodName("~").withValue(null).buildInstanceEvent();
        router.reverseRoute(destroyEvent, null);
    }

    private void respondToDestructRequest(String wisperInstanceIdentifier)
    {
        if (message==null)
            return;

        Response response = message.createResponse();
        response.setResult(wisperInstanceIdentifier);
        if (message.getResponseBlock() != null)
        {
            message.getResponseBlock().perform(response, null);
        }
    }

}


