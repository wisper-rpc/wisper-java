package com.widespace.wisper.route;


import com.widespace.wisper.annotations.WisperClassRegistry;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.WisperEventBuilder;
import com.widespace.wisper.messagetype.error.*;
import com.widespace.wisper.messagetype.error.Error;
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
    public void routeMessage(AbstractMessage message, String path) throws WisperException
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
                handleStaticEvent(message);
                break;
            case INSTANCE_EVENT:
                handleInstanceEvent(message);
                break;
            case UNKNOWN:
            default:
                super.routeMessage(message, path);
                break;
        }
    }

    //=====================================================================================
    //region Construct and Destruct
    //=====================================================================================
    private void createInstance(AbstractMessage message) throws WisperException
    {
        new WisperInstanceConstructor(wisperClassModel, message).create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                if (ex != null)
                    throw ex;

                saveInstance(instanceModel);
            }
        });
    }

    private void destroyInstance(AbstractMessage message) throws WisperException
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

    private void callStaticMethod(AbstractMessage message) throws WisperException
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    private void callInstancecMethod(AbstractMessage message) throws WisperException
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    //=====================================================================================
    //region Events
    //=====================================================================================
    private void handleStaticEvent(AbstractMessage message) throws WisperException
    {
        new WisperEventHandler(this, wisperClassModel, message).handle();
    }

    private void handleInstanceEvent(AbstractMessage message) throws WisperException
    {
        new WisperEventHandler(this, wisperClassModel, message).handle();
    }

    //=====================================================================================
    //region Other
    //=====================================================================================
    public WisperInstanceModel addInstance(Wisper anInstance)
    {
        WisperInstanceModel instanceModel = new WisperInstanceModel(wisperClassModel, anInstance, anInstance.toString());
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, this);

        HashMap<String, Object> idAndProps = new HashMap<String, Object>();
        idAndProps.put("id", instanceModel.getInstanceIdentifier());
        idAndProps.put("props", ClassUtils.fetchInitializedProperties(instanceModel, wisperClassModel));
        Event event = new WisperEventBuilder().withName("~").withValue(idAndProps).buildStaticEvent();

        reverseRoute(event, null);

        return instanceModel;
    }

    public void removeInstance(WisperInstanceModel instanceModel)
    {
        new WisperInstanceDestructor(this).destroy(instanceModel.getInstanceIdentifier());
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
