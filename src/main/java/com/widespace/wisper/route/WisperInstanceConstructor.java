package com.widespace.wisper.route;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.utils.ClassUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import static com.widespace.wisper.messagetype.error.Error.NOT_ALLOWED;
import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;
import static com.widespace.wisper.messagetype.error.Error.WISPER_INSTANCE_INVALID;

/**
 * This class specifically tries to create an instance of a remote object using the message.
 */
public class WisperInstanceConstructor
{
    private final WisperClassModel classModel;
    private final Request request;
    private final ClassRouter classRouter;

    public WisperInstanceConstructor(@NotNull ClassRouter classRouter, @NotNull WisperClassModel classModel, @NotNull AbstractMessage message)
    {
        if (MessageParser.getCallType(message) != WisperCallType.CREATE_INSTANCE)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance creator was called with a non-CREATE message type.");

        this.request = (Request) message;
        this.classModel = classModel;
        this.classRouter = classRouter;
    }

    public void create(@NotNull RemoteInstanceCreatorCallback callback)
    {
        try
        {
            if (handleBlockConstructor(callback))
            {
                callback.result(null, new WisperException(NOT_ALLOWED, null, "Block constructors are not allowed at this point."));
                return;
            }

            Class<?> classRef = classModel.getClassRef();
            Wisper instance = createInstanceWithReflection(classRef);
            setClassRouterOnInstance(instance);
            WisperInstanceModel instanceModel = createInstanceModel(instance);
            callback.result(instanceModel, null);
            respondToCreateInstanceRequest(request, instanceModel);
        } catch (WisperException e)
        {
            callback.result(null, e);
        }
    }

    private void setClassRouterOnInstance(Wisper instance) throws WisperException
    {
        try
        {
            Method setClassRouterMethod = instance.getClass().getMethod("setClassRouter", ClassRouter.class);
            setClassRouterMethod.setAccessible(true);
            setClassRouterMethod.invoke(instance, this.classRouter);

        } catch (NoSuchMethodException e)
        {
            String errorMessage = "This should never happen! Instance creation did not complete. Method setClassRouter could not be found on this class: " + classModel.getClassRef();
            throw new WisperException(Error.METHOD_NOT_FOUND, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "This should never happen! Instance creation did not complete. Method setClassRouter was found could not be invoked on this class: " + classModel.getClassRef();
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "This should never happen! Instance creation did not complete. Method setClassRouter was found could not be accessed on this class: " + classModel.getClassRef();
            throw new WisperException(Error.METHOD_NOT_ACCESSIBLE, e, errorMessage);
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
        Response response = req.createResponse();
        HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
        idWithProperties.put("id", instanceModel.getInstanceIdentifier());
        idWithProperties.put("props", ClassUtils.fetchInitializedProperties(instanceModel, classModel));
        response.setResult(idWithProperties);

        if (req.getResponseBlock() != null)
        {
            req.getResponseBlock().perform(response, null);
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
                WisperMethod newMethodModel = null;
                newMethodModel = handleCustomConstructorsAndReplaceInstanceParams(newMethodModel);

                Object[] params = MessageParser.getParams(request);
                if (newMethodModel != null)
                {
                    params = newMethodModel.getCallParameters();
                }

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

    private WisperMethod handleCustomConstructorsAndReplaceInstanceParams(WisperMethod methodModel)
    {
        WisperMethod newMethodModel = methodModel;
        if (classModel.getStaticMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod constructorMethod = classModel.getStaticMethods().get(Constants.CONSTRUCTOR_TOKEN);
            Object[] messageParams = MessageParser.getParams(request);
            newMethodModel = replaceWisperInstanceParametersWithRealInstances(constructorMethod, messageParams);

        } else if (classModel.getInstanceMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod constructorMethod = classModel.getInstanceMethods().get(Constants.CONSTRUCTOR_TOKEN);
            Object[] messageParams = MessageParser.getParams(request);
            newMethodModel = replaceWisperInstanceParametersWithRealInstances(constructorMethod, messageParams);
        }
        return newMethodModel;
    }

    private WisperMethod replaceWisperInstanceParametersWithRealInstances(WisperMethod methodModel, Object[] messageParams)
    {
        Object[] resultedParameters = Arrays.copyOf(messageParams, messageParams.length);
        Class[] parameterTypes = methodModel.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            //In case of instance, it has been replaced by RPCMethodParameter type.
            if (parameterTypes[i].equals(WisperParameterType.INSTANCE.getClass()))
            {
                WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId((String) messageParams[i]);
                Wisper instance=null;
                if(instanceModel!=null)
                    instance = instanceModel.getInstance();

                if (instance == null)
                    throw new WisperException(WISPER_INSTANCE_INVALID, null, "No such instance found with instance identifier :'" + messageParams[i] + "' passed as a parameter to method " + methodModel.getMethodName());

                resultedParameters[i] = instance;
                parameterTypes[i] = instance.getClass();
            }
        }

        methodModel.setCallParameters(resultedParameters);
        methodModel.setCallParameterTypes(parameterTypes);
        return methodModel;
    }

}

