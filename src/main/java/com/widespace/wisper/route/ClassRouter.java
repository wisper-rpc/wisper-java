package com.widespace.wisper.route;


import com.widespace.wisper.annotations.RPCClassRegistry;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

public class ClassRouter extends Router
{
    private WisperClassModel wisperClassModel;

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
                break;
            case STATIC_EVENT:
                break;
            case INSTANCE_METHOD:
                break;
            case INSTANCE_EVENT:
                break;
            case UNKNOWN:
            default:
                super.routeMessage(message, path);
                break;
        }
    }

    private void destroyInstance(AbstractMessage message)
    {
        WisperInstanceRegistry.sharedInstance().clear();
    }



    //=====================================================================================
    //region Construct
    //=====================================================================================
    private void createInstance(AbstractMessage message) throws WisperException
    {
        new WisperInstanceCreator(wisperClassModel, message).create(new RemoteInstanceCreatorCallback()
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

    private void saveInstance(WisperInstanceModel instanceModel)
    {
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, this);
    }


}
