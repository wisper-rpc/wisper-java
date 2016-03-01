package com.widespace.wisper.route;


import com.widespace.wisper.annotations.RPCClassRegistry;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

public class ClassRouter extends Router
{
    private WisperClassModel wisperClassModel;


    public ClassRouter(WisperClassModel classModel)
    {
        wisperClassModel = classModel;
    }

    public ClassRouter(@NotNull Class<? extends Wisper> clazz)
    {
        wisperClassModel = RPCClassRegistry.register(clazz);
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

    private void destroyInstance(AbstractMessage message)
    {
        new WisperInstanceDestructor(message).destroy();
    }

    private void saveInstance(WisperInstanceModel instanceModel)
    {
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, this);
    }

    //=====================================================================================
    //region Method Calls
    //=====================================================================================

    private void callStaticMethod(AbstractMessage message)
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    private void callInstancecMethod(AbstractMessage message)
    {
        new WisperMethodCaller(this, wisperClassModel, message).call();
    }

    //=====================================================================================
    //region Events
    //=====================================================================================
    private void handleStaticEvent(AbstractMessage message)
    {
        new WisperEventHandler(this, wisperClassModel, message).handle();
    }
    private void handleInstanceEvent(AbstractMessage message)
    {

    }

}
