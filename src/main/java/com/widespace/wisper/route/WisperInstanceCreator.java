package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static com.widespace.wisper.messagetype.error.Error.NOT_ALLOWED;
import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;

/**
 * This class specifically tries to create an instance of a remote object using the message.
 */
public class WisperInstanceCreator
{
    private final WisperClassModel classModel;
    private final Request request;

    public WisperInstanceCreator(@NotNull WisperClassModel classModel, @NotNull AbstractMessage message)
    {
        if (MessageParser.getCallType(message) != WisperCallType.CREATE_INSTANCE)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance creator was called with a non-CREATE message type.");

        this.request = (Request) message;
        this.classModel = classModel;
    }

    public void create(@NotNull RemoteInstanceCreatorCallback callback)
    {
        try
        {
            if(handleBlockConstructor(callback))
            {
                callback.result(null, new WisperException(NOT_ALLOWED, null, "Block constructors are not allowed at this point."));
                return;
            }

            Class<?> classRef = classModel.getClassRef();
            Wisper instance = createInstanceWithReflection(classRef);
            WisperInstanceModel instanceModel = createInstanceModel(instance);
            callback.result(instanceModel, null);
            respondToCreateInstanceRequest(request, instanceModel);
        } catch (WisperException e)
        {
            callback.result(null, e);
        }
    }

    private boolean handleBlockConstructor(RemoteInstanceCreatorCallback callback)
    {
        //TODO: For now, constructor blocks are not allowed - implement later
        return false;
    }

    private WisperInstanceModel createInstanceModel(Wisper instance)
    {
        return new WisperInstanceModel(classModel, instance, instance.toString());
    }


    private void respondToCreateInstanceRequest(Request req, WisperInstanceModel instanceModel)
    {

        //instanceMap.put(nativeInstanceId, wisperInstanceModel);


        if (req != null)
        {
            Response response = req.createResponse();
            HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
            idWithProperties.put("id", instanceModel.getInstanceIdentifier());
            idWithProperties.put("props", fetchInitializedProperties(instanceModel));
            response.setResult(idWithProperties);

            if (req.getResponseBlock() != null)
            {
                req.getResponseBlock().perform(response, null);
            }
        }
    }

    private Wisper createInstanceWithReflection(Class<?> classRef) throws WisperException
    {
        try
        {
            //If constructor has parameters it must be handled
            if (MessageParser.hasParams(request))
            {
                Class<?> aClass = Class.forName(classRef.getName());
                Object[] params = MessageParser.getParams(request);
                Constructor<?> constructor = aClass.getConstructor(ClassUtils.getParameterClasses(params));
                return (Wisper) constructor.newInstance(params);
            } else
            {
                return (Wisper) Class.forName(classRef.getName()).newInstance();
            }
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Could not invoked on this class. " + classModel.getClassRef();
            throw new WisperException(Error.CONSTRUCTOR_NOT_INVOKED, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Could not access the specified constructor for this class. " + classModel.getClassRef() + ". Is the constructor public?";
            throw new WisperException(Error.CONSTRUCTOR_NOT_ACCESSIBLE, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Could not find the specified constructor for this class. " + classModel.getClassRef() + ". Are the arguments passed correct?";
            throw new WisperException(Error.CONSTRUCTOR_NOT_FOUND, e, errorMessage);
        } catch (ClassNotFoundException e)
        {
            String errorMessage = "Could not find this class. " + classModel.getClassRef() + "Has the class been registered properly?";
            throw new WisperException(Error.NATIVE_CLASS_NOT_FOUND, e, errorMessage);
        } catch (InstantiationException e)
        {
            String errorMessage = "Could not instantiate this class. " + classModel.getClassRef() + ". Is the class Abstract?";
            throw new WisperException(Error.INSTANTIATION_ERROR, e, errorMessage);
        }
    }


    private Object fetchInitializedProperties(WisperInstanceModel wisperInstanceModel)
    {
        return null;
    }
}

