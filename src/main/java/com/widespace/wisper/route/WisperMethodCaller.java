package com.widespace.wisper.route;

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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;
import static com.widespace.wisper.messagetype.error.Error.WISPER_INSTANCE_INVALID;

public class WisperMethodCaller
{
    private ClassRouter router;
    private WisperClassModel classModel;
    private AbstractMessage message;

    public WisperMethodCaller(@NotNull ClassRouter router, @NotNull WisperClassModel classModel, @NotNull AbstractMessage message) throws WisperException
    {
        this.router = router;
        this.classModel = classModel;
        this.message = message;
        WisperCallType callType = MessageParser.getCallType(message);
        if (callType != WisperCallType.STATIC_METHOD && callType != WisperCallType.INSTANCE_METHOD)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Wisper method caller accepts messages of instance or static method calls only. Call type of the message was: " + callType.toString());
    }

    public void call() throws WisperException
    {
        WisperMethod wisperMethod;

        String methodName = MessageParser.getMethodName(message);
        switch (MessageParser.getCallType(message))
        {
            case STATIC_METHOD:
            {
                wisperMethod = classModel.getStaticMethods().get(methodName);
                callStatic(wisperMethod);
            }
            break;
            case INSTANCE_METHOD:
            {
                wisperMethod = classModel.getInstanceMethods().get(methodName);
                callInstance(wisperMethod);
            }
            break;
            default:
                // No method call, so return
                break;
        }
    }


    public void callStatic(WisperMethod methodModel) throws WisperException
    {
        handleUndefinedMethods(methodModel);
        if (handledMethodCallBlock(methodModel, null))
            return;

        Object[] messageParams = MessageParser.getParams(message);
        WisperMethod newMethodModel = replaceWisperInstanceParametersWithRealInstances(methodModel, messageParams);
        callMethodOnInstance(null, newMethodModel);
    }

    public void callInstance(WisperMethod methodModel) throws WisperException
    {
        handleUndefinedMethods(methodModel);
        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        WisperInstanceModel wisperInstance = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier);
        if (handledMethodCallBlock(methodModel, wisperInstance))
            return;

        Object[] messageParams = MessageParser.getParams(message);
        WisperMethod newMethodModel = replaceWisperInstanceParametersWithRealInstances(methodModel, messageParams);
        callMethodOnInstance(wisperInstance, newMethodModel);
    }

    private boolean handledMethodCallBlock(WisperMethod methodModel, WisperInstanceModel wisperInstance)
    {
        try
        {
            if (methodModel.getCallBlock() != null)
            {
                methodModel.getCallBlock().perform(router, wisperInstance, methodModel, (Request) message);
                return true;
            }

            return false;

        } catch (Exception e)
        {
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, "An exception happened while trying to invoke callBlock on method " + methodModel.getMethodName() + ". Exception : " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void handleUndefinedMethods(WisperMethod methodModel) throws WisperException
    {
        if (methodModel == null)
        {
            String errorMessage = "Method " + MessageParser.getMethodName(message) + " could not be found on class " + classModel.getClassRef();
            throw new WisperException(Error.METHOD_NOT_FOUND, null, errorMessage);
        }
    }

    private void callMethodOnInstance(WisperInstanceModel wisperInstance, WisperMethod methodModel) throws WisperException
    {
        String methodName = methodModel.getMethodName();
        Class[] parameterTypes = methodModel.getCallParameterTypes();
        Object[] params = methodModel.getCallParameters();


        Method method;
        Object returnedValue;


        try
        {
            if (wisperInstance != null)
            {
                Wisper instance = wisperInstance.getInstance();

                // Instance method
                method = getMethod(instance.getClass(), methodName, parameterTypes);
                method.setAccessible(true);
                returnedValue = method.invoke(instance, params);
            } else
            {
                // Static method
                method = getMethod(classModel.getClassRef(), methodName, parameterTypes);
                method.setAccessible(true);
                returnedValue = method.invoke(null, params);
            }

            if (message instanceof Request)
            {
                Response response = ((Request) message).createResponse();
                if (returnedValue != null)
                {
                    response.setResult(returnedValue);
                }

                if (((Request) message).getResponseBlock() != null)
                {
                    ((Request) message).getResponseBlock().perform(response, null);
                }
            }
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Method " + methodName + " exists but is not accessible on wisper class " + classModel.getClassRef() + ". Is the method public?";
            throw new WisperException(Error.METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClassRef() + ". Are passed parameters of correct type?";
            throw new WisperException(Error.METHOD_INVALID_ARGUMENTS, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClassRef();
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Method " + methodName + " could not be found on class " + classModel.getClassRef() + "with argument types " + Arrays.toString(parameterTypes) + ". Is the method defined in the class?";
            throw new WisperException(Error.METHOD_NOT_FOUND, e, errorMessage);
        }
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
                Wisper instance = WisperInstanceRegistry.sharedInstance().findInstanceWithId((String) messageParams[i]).getInstance();
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

    private Method getMethod(Class<?> clasRef, String methodName, Class[] parameterTypes) throws NoSuchMethodException
    {
        nextMethod:
        for (Method method : clasRef.getMethods())
        {
            if (!methodName.equals(method.getName()))
            {
                continue;
            }

            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (methodParameterTypes.length != parameterTypes.length)
            {
                continue;
            }

            for (int i = 0; i < methodParameterTypes.length; i++)
            {

                if (!methodParameterTypes[i].isAssignableFrom(parameterTypes[i]))
                {
                    continue nextMethod;
                }
            }

            return method;
        }

        throw new NoSuchMethodException();
    }

    private void checkParameterTypes(Class<?>[] parameterTypes, Object[] params) throws IllegalArgumentException
    {
        if (parameterTypes.length == params.length && parameterTypes.length > 0)
        {
            for (int i = 0; i < parameterTypes.length; i++)
            {

                if (!(params[i].getClass().equals(parameterTypes[i]) || Number.class.isAssignableFrom(params[i].getClass())))
                {
                    throw new IllegalArgumentException("Argument types passed do not match with the registered method arguments, argument %d was of type "
                            + params[i].getClass().getSimpleName()
                            + " but " + parameterTypes[i].getSimpleName() + "was expected.");
                }
            }
        }
    }
}


