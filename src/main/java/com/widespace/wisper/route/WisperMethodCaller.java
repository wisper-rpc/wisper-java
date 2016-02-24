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

enum CallMode
{
    STATIC,
    INSTANCE
}

public class WisperMethodCaller
{
    private WisperClassModel classModel;
    private AbstractMessage message;

    public WisperMethodCaller(@NotNull WisperClassModel classModel, @NotNull AbstractMessage message) throws WisperException
    {
        this.classModel = classModel;
        this.message = message;
        WisperCallType callType = MessageParser.getCallType(message);
        if (callType != WisperCallType.STATIC_METHOD && callType != WisperCallType.INSTANCE_METHOD)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Wisper method caller accepts messages of instance or static method calls only. Call type of the message was: " + callType.toString());


    }

    public void call() throws WisperException
    {
        WisperMethod wisperMethod = null;

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
                return;
        }
    }


    public void callStatic(WisperMethod methodModel) throws WisperException
    {
        if (methodModel == null)
        {
            String errorMessage = "Method " + MessageParser.getMethodName(message) + " could not be found on class " + classModel.getClass().getName();
            throw new WisperException(Error.METHOD_NOT_FOUND, null, errorMessage);
        }

        //If there is a call block instead of the method body there, just handle the call block and return.
        if (methodModel.getCallBlock() != null)
        {
            //methodModel.getCallBlock().perform(this, wisperInstance, wisperMethod, remoteObjectCall.getRequest());
            return;
        }

        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        WisperInstanceModel wisperInstance = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier);
        String methodName = methodModel.getMethodName();


        //Replace INSTANCE with actual instance type in params
        Class[] parameterTypes = methodModel.getParameterTypes();
        Object[] params = MessageParser.getParams(message);

        Method method;
        Object returnedValue;

        replaceWisperInstanceParametersWithRealInstances(instanceIdentifier, parameterTypes, params);

        try
        {
            if (wisperInstance != null)
            {
                Wisper instance = wisperInstance.getInstance();

                // Instance method
                method = getMethod(instance.getClass(), methodName, parameterTypes);
                method.setAccessible(true);
                checkParameterTypes(parameterTypes, params);
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
            String errorMessage = "Method " + methodName + " exists but is not accessible on wisper class " + classModel.getClass().getName() + ". Is the method public?";
            throw new WisperException(Error.METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClass().getName() + ". Are passed parameters of correct type?";
            throw new WisperException(Error.METHOD_INVALID_ARGUMENTS, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClass().getName();
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Method " + methodName + " could not be found on class " + classModel.getClass().getName() + "with argument types " + Arrays.toString(parameterTypes) + ". Is the method defined in the class?";
            throw new WisperException(Error.METHOD_NOT_FOUND, e, errorMessage);
        }
    }

    public void callInstance(WisperMethod methodModel)
    {
        if (methodModel == null)
        {
            String errorMessage = "Method " + MessageParser.getMethodName(message) + " could not be found on class " + classModel.getClass().getName();
            throw new WisperException(Error.METHOD_NOT_FOUND, null, errorMessage);
        }

        //If there is a call block instead of the method body there, just handle the call block and return.
        if (methodModel.getCallBlock() != null)
        {
            //methodModel.getCallBlock().perform(this, wisperInstance, wisperMethod, remoteObjectCall.getRequest());
            return;
        }

        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        WisperInstanceModel wisperInstance = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier);
        String methodName = methodModel.getMethodName();


        //Replace INSTANCE with actual instance type in params
        Class[] parameterTypes = methodModel.getParameterTypes();
        Object[] params = MessageParser.getParams(message);

        Method method;
        Object returnedValue;

        replaceWisperInstanceParametersWithRealInstances(instanceIdentifier, parameterTypes, params);

        try
        {
            if (wisperInstance != null)
            {
                Wisper instance = wisperInstance.getInstance();

                // Instance method
                method = getMethod(instance.getClass(), methodName, parameterTypes);
                method.setAccessible(true);
                checkParameterTypes(parameterTypes, params);
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
            String errorMessage = "Method " + methodName + " exists but is not accessible on wisper class " + classModel.getClass().getName() + ". Is the method public?";
            throw new WisperException(Error.METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClass().getName() + ". Are passed parameters of correct type?";
            throw new WisperException(Error.METHOD_INVALID_ARGUMENTS, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + classModel.getClass().getName();
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Method " + methodName + " could not be found on class " + classModel.getClass().getName() + "with argument types " + Arrays.toString(parameterTypes) + ". Is the method defined in the class?";
            throw new WisperException(Error.METHOD_NOT_FOUND, e, errorMessage);
        }
    }

    private void replaceWisperInstanceParametersWithRealInstances(String instanceIdentifier, Class[] parameterTypes, Object[] params)
    {
        for (int i = 0; i < parameterTypes.length; i++)
        {
            //In case of instance, it has been replaced by RPCMethodParameter type.
            if (parameterTypes[i].equals(WisperParameterType.INSTANCE.getClass()))
            {
                Wisper instance = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier).getInstance();
                params[i] = instance;
                parameterTypes[i] = instance.getClass();
            }
        }
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


