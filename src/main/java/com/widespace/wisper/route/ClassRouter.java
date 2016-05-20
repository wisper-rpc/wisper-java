package com.widespace.wisper.route;


import com.widespace.wisper.annotations.WisperClassRegistry;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ClassRouter extends Router
{
    private WisperClassModel wisperClassModel;


    public ClassRouter(WisperClassModel classModel)
    {
        wisperClassModel = classModel;
    }

    public ClassRouter(@NotNull Class<? extends Wisper> clazz)
    {
        wisperClassModel = WisperClassRegistry.register(clazz);
    }

    public WisperClassModel getWisperClassModel()
    {
        return wisperClassModel;
    }


    @Override
    public void routeMessage(CallMessage message, String path) throws WisperException
    {
        switch (MessageParser.getCallType(message))
        {
            case CREATE_INSTANCE:
                createInstance(message);
                break;
            case DESTROY_INSTANCE:
                destroyInstance(message);
                break;
            case STATIC_METHOD:
                callStaticMethod(message);
                break;
            case INSTANCE_METHOD:
                callInstancecMethod(message);
                break;
            case STATIC_EVENT:
                handleStaticEvent(new Event((Notification) message));
                break;
            case INSTANCE_EVENT:
                handleInstanceEvent(new Event((Notification) message));
                break;
            /* TODO: What's this all about?
            case UNKNOWN:
            default:
                super.routeMessage(message, path);
                break;
            */
        }
    }

    //=====================================================================================
    //region Construct and Destruct
    //=====================================================================================
    private void createInstance(CallMessage message) throws WisperException
    {
        new WisperInstanceConstructor(this, wisperClassModel, message).create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                if (ex != null)
                {
                    throw ex;
                }

                saveInstance(instanceModel);
            }
        });
    }

    private void destroyInstance(CallMessage message) throws WisperException
    {
        new WisperInstanceDestructor(message, this).destroy();
    }

    private void saveInstance(WisperInstanceModel instanceModel)
    {
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, getRootRoute());
    }

    //=====================================================================================
    //region Method Calls
    //=====================================================================================

    private void callStaticMethod(CallMessage message) throws WisperException
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    private void callInstancecMethod(CallMessage message) throws WisperException
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    //=====================================================================================
    //region Events
    //=====================================================================================
    private void handleStaticEvent(Event event) throws WisperException
    {
        new WisperEventHandler(wisperClassModel, event).handle();
    }

    private void handleInstanceEvent(Event event) throws WisperException
    {
        new WisperEventHandler(wisperClassModel, event).handle();
    }

    //=====================================================================================
    //region Other
    //=====================================================================================
    public WisperInstanceModel addInstance(Wisper anInstance)
    {
        WisperInstanceModel instanceModel = new WisperInstanceModel(wisperClassModel, anInstance, anInstance.toString());
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, this.getRootRoute());
        anInstance.setClassRouter(this);

        HashMap<String, Object> idAndProps = new HashMap<String, Object>();
        idAndProps.put("id", instanceModel.getInstanceIdentifier());
        idAndProps.put("props", ClassUtils.fetchInitializedProperties(instanceModel, wisperClassModel));
        Event event = new WisperEventBuilder().withName("~").withValue(idAndProps).buildStaticEvent();

        reverseRoute(event, null);

        return instanceModel;
    }

    public void removeInstance(WisperInstanceModel instanceModel)
    {
        Notification destructNotification = new Notification("created.by.router:~", new Object[]{instanceModel.getInstanceIdentifier()});
        new WisperInstanceDestructor(destructNotification, this).destroy();
    }

    public Gateway getRootGateway()
    {
        Router rootRoute = getRootRoute();
        if (rootRoute instanceof GatewayRouter)
        {
            return ((GatewayRouter) rootRoute).getGateway();
        }

        throw new WisperException(Error.UNKNOWN_ERROR, null, "Could not retrieve a gateway from the root router because the root is not a GatewayRouter.");
    }

    public Object getGatewayExtra(String extraKey) throws WisperException
    {
        Router rootRoute = getRootRoute();
        if (rootRoute instanceof GatewayRouter)
        {
            return ((GatewayRouter) rootRoute).getGateway().getExtra(extraKey);
        }

        throw new WisperException(Error.UNKNOWN_ERROR, null, "Could not retrieve the value for " + extraKey + "Root of this class router is not a GatewayRouter.");
    }
}
