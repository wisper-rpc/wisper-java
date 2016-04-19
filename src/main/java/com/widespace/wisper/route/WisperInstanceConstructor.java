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

import static com.widespace.wisper.messagetype.error.Error.*;

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
            Class<?> classRef = classModel.getClassRef();
            Wisper instance = createInstanceWithReflection(classRef);
            setClassRouterOnInstance(instance);
            WisperInstanceModel instanceModel = createInstanceModel(instance);
            if (handleBlockConstructor(instanceModel, callback))
            {
                return;
            }

            callback.result(instanceModel, null);
            respondToCreateInstanceRequest(request, instanceModel);

        } catch (WisperException e)
        {
            callback.result(null, e);
        } catch (Exception ex)
        {
            callback.result(null, new WisperException(UNKNOWN_ERROR, ex, "An unknown exception happened while trying to call the constructor."));
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

    private boolean handleBlockConstructor(WisperInstanceModel instanceModel, RemoteInstanceCreatorCallback callback) throws Exception
    {
        if (classModel.getStaticMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod wisperMethod = classModel.getStaticMethods().get(Constants.CONSTRUCTOR_TOKEN);
            if (wisperMethod.getCallBlock() != null)
            {
                wisperMethod.getCallBlock().perform(classRouter, instanceModel, wisperMethod, request);
                callback.result(instanceModel, null);
                //respondToCreateInstanceRequest(request, instanceModel);
                return true;
            }
        }

        if (classModel.getInstanceMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod wisperMethod = classModel.getInstanceMethods().get(Constants.CONSTRUCTOR_TOKEN);
            if (wisperMethod.getCallBlock() != null)
            {
                wisperMethod.getCallBlock().perform(classRouter, instanceModel, wisperMethod, request);
                callback.result(instanceModel, null);
                //respondToCreateInstanceRequest(request, instanceModel);
                return true;
            }
        }

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
                WisperMethod customConstructor = handleCustomConstructorsAndReplaceInstanceParams(classModel);

                Object[] params;
                if (customConstructor != null)
                {
                    //Custom constructor
                    Constructor<?> constructor = getConstructorForArgs(aClass, customConstructor.getCallParameterTypes()); //aClass.getConstructor(customConstructor.getCallParameterTypes());
                    return (Wisper) constructor.newInstance(customConstructor.getCallParameters());
                } else
                {
                    //constructor with params
                    params = MessageParser.getParams(request);
                    Constructor<?> constructor = getConstructorForArgs(aClass, ClassUtils.getParameterClasses(params)); //aClass.getConstructor(ClassUtils.getParameterClasses(params));
                    return (Wisper) constructor.newInstance(params);
                }

            } else
            {
                //default constructor without any params
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
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Could not find the specified constructor for this class. " + classModel.getClassRef() + ". Are the arguments passed correct?";
            throw new WisperException(Error.CONSTRUCTOR_NOT_FOUND, e, errorMessage);
        }
    }


    private WisperMethod handleCustomConstructorsAndReplaceInstanceParams(WisperClassModel theClassModel)
    {
        WisperMethod newMethodModel = null;
        if (theClassModel.getStaticMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod constructorMethod = theClassModel.getStaticMethods().get(Constants.CONSTRUCTOR_TOKEN);
            Object[] messageParams = MessageParser.getParams(request);
            newMethodModel = replaceWisperInstanceParametersWithRealInstances(constructorMethod, messageParams);

        } else if (theClassModel.getInstanceMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
        {
            WisperMethod constructorMethod = theClassModel.getInstanceMethods().get(Constants.CONSTRUCTOR_TOKEN);
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
                Wisper instance = null;
                if (instanceModel != null)
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


    private Constructor<?> getConstructorForArgs(Class<?> klass, Class[] args) throws NoSuchMethodException
    {
        //Get all the constructors from given class
        Constructor<?>[] constructors = klass.getConstructors();

        for (Constructor<?> constructor : constructors)
        {
            //Walk through all the constructors, matching parameter amount and parameter types with given types (args)
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == args.length)
            {
                boolean argumentsMatch = false;
                for (int i = 0; i < args.length; i++)
                {
                    //Note that the types in args must be in same order as in the constructor if the checking is done this way
                    if (types[i].isAssignableFrom(args[i]))
                    {
                        argumentsMatch = true;
                        break;
                    }
                }

                if (argumentsMatch)
                {
                    //We found a matching constructor, return it
                    return constructor;
                }
            }
        }

        //No matching constructor
        throw new NoSuchMethodException("Constructor with parameters " + Arrays.toString(args) + " was not found on " + klass.toString());
    }

}

