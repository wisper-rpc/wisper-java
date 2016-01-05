package com.widespace.wisper.controller;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.RPCEventBuilder;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.*;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Subclass of the Gateway to extend functionality for handling instances
 * with rpc messages. This class will allow you to register classes to be used
 * by the rpc bridge through exposed methods.
 * <p/>
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RemoteObjectController extends Gateway
{
    HashMap<String, WisperClassModel> classMap;
    HashMap<String, WisperInstanceModel> instanceMap;
    private WisperExceptionHandler wisperExceptionHandler;

    public RemoteObjectController(GatewayCallback callback)
    {
        super(callback);
        classMap = new HashMap<String, WisperClassModel>();
        instanceMap = new HashMap<String, WisperInstanceModel>();
    }

    /**
     * Registers an already modeled class into the classes map
     *
     * @param clazz RPC model class
     */
    public void registerClass(WisperClassModel clazz)
    {
        classMap.put(clazz.getMapName(), clazz);
    }

    /**
     * Returns a registered RPC class model for the class. If no class model can
     * be found null will be returned.
     *
     * @param clazz the class name
     * @return RPC class model object representing clazz
     */
    public WisperClassModel getRpcClassForClass(Class<?> clazz)
    {
        if (classMap != null && classMap.keySet().size() > 0)
        {
            for (String key : classMap.keySet())
            {
                WisperClassModel wisperClassModel = classMap.get(key);
                if (wisperClassModel.getClassRef().equals(clazz))
                {
                    return wisperClassModel;
                }
            }
        }

        return null;
    }

    /**
     * Returns an RPC instance model for an instantiated and mapped class that
     * is owned by this RPC Controller.
     *
     * @param instance an instance of a class that implements the RPC protocol
     *                 and has the static registerClass() method implemented.
     */
    public WisperInstanceModel getWisperClassInstance(Wisper instance)
    {
        return getWisperClassInstance(instance.toString());
    }

    public WisperInstanceModel getWisperClassInstance(String instanceIdentifier)
    {
        return instanceMap.get(instanceIdentifier);
    }

    /**
     * clears out all instances in instance map
     */
    public void flushInstances()
    {
        for (String key : instanceMap.keySet())
        {
            instanceMap.get(key).getInstance().destruct();
        }
        instanceMap.clear();
    }

    /**
     * Returns all instances currently under control of this remote object controller.
     *
     * @return instances.
     */
    public HashMap<String, WisperInstanceModel> getInstanceMap()
    {
        return instanceMap;
    }

    /**
     * Add an existing instance under this remote object controller.
     *
     * @param wisperInstance an existing instance.
     * @param wisperClassModel       class model.
     * @return the wisper class instance
     */
    public WisperInstanceModel addRpcObjectInstance(Wisper wisperInstance, WisperClassModel wisperClassModel)
    {
        wisperInstance.setRemoteObjectController(this);
        String key = wisperInstance.toString();
        WisperInstanceModel wisperInstanceModel = new WisperInstanceModel(wisperClassModel, wisperInstance, key);
        instanceMap.put(key, wisperInstanceModel);
        return wisperInstanceModel;
    }

    /**
     * Removes an instance from the remote object controller.
     *
     * @param wisperInstanceModel the wisper class instance to be removed.
     * @return true if removed, false of instance did not exist.
     */
    public boolean removeRpcObjectInstance(WisperInstanceModel wisperInstanceModel)
    {
        if (instanceMap.containsValue(wisperInstanceModel))
        {
            wisperInstanceModel.getInstance().setRemoteObjectController(null);
            instanceMap.remove(wisperInstanceModel);
            return true;
        }

        return false;
    }

    /**
     * Sends a wisper event to an instance.
     *
     * @param rpcInstance the instance to which the event is sent.
     * @param key         event key.
     * @param value       the value wrapped in the event.
     */
    public void sendInstanceEvent(Wisper rpcInstance, String key, Object value)
    {
        WisperInstanceModel wisperInstanceModel = getWisperClassInstance(rpcInstance);

        if (wisperInstanceModel == null)
        {
            return;
        }

        String instanceIdentifier = wisperInstanceModel.getInstanceIdentifier();

        WisperClassModel wisperClassModel = getRpcClassForClass(rpcInstance.getClass());
        String mapName = wisperClassModel.getMapName();

        Event event = new RPCEventBuilder().withInstanceIdentifier(instanceIdentifier).withMethodName(mapName).withName(key).withValue(value).buildInstanceEvent();
        sendMessage(event);
    }

    @Override
    public void handleMessage(AbstractMessage message)
    {
        try
        {
            super.handleMessage(message);
            RemoteObjectCall remoteObjectCall = new RemoteObjectCall(message);
            wisperExceptionHandler = new WisperExceptionHandler(this, remoteObjectCall);
            makeCall(remoteObjectCall);
        } catch (WisperException ex)
        {
            wisperExceptionHandler.handle(ex);
        }
    }

    // Private Methods
    private void makeCall(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        switch (remoteObjectCall.getCallType())
        {
            case CREATE:
                createRemoteObject(remoteObjectCall);
                break;
            case DESTROY:
                rpcRemoteObjectDestruct(remoteObjectCall);
                break;
            case STATIC:
                rpcRemoteObjectCallStaticMethod(remoteObjectCall);
                break;
            case STATIC_EVENT:
                handleStaticEvent(remoteObjectCall);
                break;
            case INSTANCE:
                rpcRemoteObjectCallInstanceMethod(remoteObjectCall);
                break;
            case INSTANCE_EVENT:
                handleInstanceEvent(remoteObjectCall);
                break;
            case UNKNOWN:
            default:
                break;
        }
    }


    //=====================================================================================
    //region Events
    //=====================================================================================
    private void handleStaticEvent(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        try
        {
            if (classMap.containsKey(remoteObjectCall.getClassName()))
            {
                WisperClassModel wisperClassModel = classMap.get(remoteObjectCall.getClassName());
                String proprtyName = (String) remoteObjectCall.getParams()[0];
                String proprtyValue = (String) remoteObjectCall.getParams()[1];
                Field field = wisperClassModel.getClassRef().getField(proprtyName);
                field.setAccessible(true);
                field.set(null, proprtyValue);
            }
        } catch (NoSuchFieldException e)
        {
            String errorMessage = "No such field defined as " + remoteObjectCall.getParams()[0] + ".Is the property registered with the class model?";
            throw new WisperException(Error.PROPERTY_NOT_REGISTERED, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Property " + remoteObjectCall.getParams()[0] + " is not accessible in class " + remoteObjectCall.getClassName() + ". Is the property public?";
            throw new WisperException(Error.PROPERTY_NOT_ACCESSIBLE, e, errorMessage);
        }
    }

    private void handleInstanceEvent(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        if (!instanceMap.containsKey(remoteObjectCall.getInstanceIdentifier()))
        {
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this controller under route :" + remoteObjectCall.getClassName());
        }

        WisperInstanceModel wisperInstanceModel = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
        handlePropertySetWithInstanceEvent(wisperInstanceModel, remoteObjectCall);
    }

    private void handlePropertySetWithInstanceEvent(WisperInstanceModel wisperInstanceModel, RemoteObjectCall remoteObjectCall) throws WisperException
    {

        Event event = new Event(remoteObjectCall);
        HashMap<String, RPCClassProperty> properties = wisperInstanceModel.getWisperClassModel().getProperties();
        if (properties == null || !properties.containsKey(event.getName()))
        {
            //RPCLogger.log(RPCLogger.LogType.SDK_2_AD, "Property is not registered with the class");
            return;
        }

        RPCClassProperty property = properties.get(event.getName());
        if (property.getMode() == WisperPropertyAccess.READ_ONLY)
        {
            //RPCLogger.log(RPCLogger.LogType.SDK_2_AD, "Insufficient access rights on property. Cannot write to the property with read only access.");
            return;
        }


        String setterMethodName = property.getSetterName();
        Wisper instance = wisperInstanceModel.getInstance();

        // Instance method
        Class[] parameterTypes = RPCUtilities.convertRpcParameterTypeToClassType(property.getSetterMethodParameterType());
        // If property is pointing to an RPC instance, replace the pointer to the actual value
        if (property.getSetterMethodParameterType() == WisperParameterType.INSTANCE)
        {
            if (instanceMap.containsKey(event.getValue().toString()))
            {
                WisperInstanceModel classInstancePointer = instanceMap.get(event.getValue().toString());
                event.setValue(classInstancePointer.getInstance());
                parameterTypes[0] = classInstancePointer.getInstance().getClass();
            }
        }
        try
        {
            Method method = getMethod(instance.getClass(), setterMethodName, parameterTypes);
            method.setAccessible(true);
            checkParameterTypes(parameterTypes, remoteObjectCall.getParams());
            method.invoke(instance, event.getValue());

        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "not found in class " + wisperInstanceModel.getWisperClassModel().getMapName() + ". Does the setter method actually exist in the class? ";
            throw new WisperException(Error.SETTER_METHOD_NOT_FOUND, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "was not accessible in class  " + wisperInstanceModel.getWisperClassModel().getMapName() + ".Is the setter method public?";
            throw new WisperException(Error.SETTER_METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "could not be invoked in class  " + wisperInstanceModel.getWisperClassModel().getMapName();
            throw new WisperException(Error.SETTER_METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "rejected the argument sent in class  " + wisperInstanceModel.getWisperClassModel().getMapName() + "Are the arguments passed correctly?";
            throw new WisperException(Error.SETTER_METHOD_WRONG_ARGUMENTS, e, errorMessage);
        }
    }


    //=====================================================================================
    //region Construct
    //=====================================================================================
    private void createRemoteObject(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        if (!classMap.containsKey(remoteObjectCall.getClassName()))
        {
            throw new WisperException(Error.ROUTE_NOT_FOUND, null, "No such class has been registered with this controller under route :" + remoteObjectCall.getClassName());
        }

        try
        {
            WisperClassModel wisperClassModel = classMap.get(remoteObjectCall.getClassName());
            Class<?> classRef = wisperClassModel.getClassRef();

            Wisper instance;

            // Make it possible for the Wisper class to override constructor using a callBlock.
            if (wisperClassModel.getInstanceMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
            {
                callRpcClassMethodOnInstance(wisperClassModel.getInstanceMethods().get("~"), null, wisperClassModel, remoteObjectCall);
                return;
            }

            if (wisperClassModel.getStaticMethods().containsKey(Constants.CONSTRUCTOR_TOKEN))
            {
                callRpcClassMethodOnInstance(wisperClassModel.getStaticMethods().get("~"), null, wisperClassModel, remoteObjectCall);
            }

            //If constructor has parameters it must be handled
            if (remoteObjectCall.getParams() != null && remoteObjectCall.getParams().length > 0)
            {
                Class<?> aClass = Class.forName(classRef.getName());
                Constructor<?> constructor = aClass.getConstructor(ClassUtils.getParameterClasses(remoteObjectCall.getParams()));
                instance = (Wisper) constructor.newInstance(remoteObjectCall.getParams());
            } else
            {
                instance = (Wisper) Class.forName(classRef.getName()).newInstance();
            }


            String nativeInstanceId = instance.toString();
            WisperInstanceModel wisperInstanceModel = new WisperInstanceModel(wisperClassModel, instance, nativeInstanceId);
            instanceMap.put(nativeInstanceId, wisperInstanceModel);
            instance.setRemoteObjectController(this);

            if (remoteObjectCall.getRequest() != null)
            {
                Response response = remoteObjectCall.getRequest().createResponse();
                HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
                idWithProperties.put("id", nativeInstanceId);
                idWithProperties.put("props", fetchInitializedProperties(wisperInstanceModel));
                response.setResult(idWithProperties);

                if (remoteObjectCall.getRequest().getResponseBlock() != null)
                {
                    remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
                }
            }
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Could not invoked on this class. " + classMap.get(remoteObjectCall.getClassName()).getClassRef();
            throw new WisperException(Error.CONSTRUCTOR_NOT_INVOKED, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Could not access the specified constructor for this class. " + classMap.get(remoteObjectCall.getClassName()).getClassRef() + ". Is the constructor public?";
            throw new WisperException(Error.CONSTRUCTOR_NOT_ACCESSIBLE, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Could not find the specified constructor for this class. " + classMap.get(remoteObjectCall.getClassName()).getClassRef() + ". Are the arguments passed correct?";
            throw new WisperException(Error.CONSTRUCTOR_NOT_FOUND, e, errorMessage);
        } catch (ClassNotFoundException e)
        {
            String errorMessage = "Could not find this class. " + classMap.get(remoteObjectCall.getClassName()).getClassRef() + "Has the class been registered properly?";
            throw new WisperException(Error.NATIVE_CLASS_NOT_FOUND, e, errorMessage);
        } catch (InstantiationException e)
        {
            String errorMessage = "Could not instantiate this class. " + classMap.get(remoteObjectCall.getClassName()).getClassRef() + ". Is the class Abstract?";
            throw new WisperException(Error.INSTANTIATION_ERROR, e, errorMessage);
        }
    }


    private HashMap<String, Object> fetchInitializedProperties(WisperInstanceModel wisperInstanceModel) throws WisperException
    {
        HashMap<String, Object> initializedProperties = new HashMap<String, Object>();
        HashMap<String, RPCClassProperty> properties = wisperInstanceModel.getWisperClassModel().getProperties();

        String currentPropertyName = null;
        try
        {
            for (String propertyName : properties.keySet())
            {
                currentPropertyName = propertyName;

                RPCClassProperty property = properties.get(propertyName);
                Wisper instance = wisperInstanceModel.getInstance();


                //Check if there is a getter implemented.
                Method getter = instance.getClass().getMethod(property.getSetterName().replace("set", "get"));
                Object value = getter.invoke(instance);

                //Check if the value is something other than null (default)
                if (value == null)
                {
                    continue;
                }

                if (property.getSetterMethodParameterType() == WisperParameterType.INSTANCE)
                {
                    WisperInstanceModel WisperInstanceModel = getWisperClassInstance((Wisper) value);
                    value = WisperInstanceModel.getInstanceIdentifier();
                }

                initializedProperties.put(propertyName, value);


            }
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + "not found in class " + wisperInstanceModel.getWisperClassModel().getMapName() + ". Does the getter method actually exist in the class? ";
            throw new WisperException(Error.GETTER_METHOD_NOT_FOUND, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + "not accessible in class " + wisperInstanceModel.getWisperClassModel().getMapName() + ". Is the getter method public?";
            throw new WisperException(Error.GETTER_METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Getter method for the property " + currentPropertyName + "could not be invoked in class  " + wisperInstanceModel.getWisperClassModel().getMapName();
            throw new WisperException(Error.GETTER_METHOD_INVOCATION_ERROR, e, errorMessage);
        }

        return initializedProperties;
    }

    //=====================================================================================
    //region Destruct
    //=====================================================================================

    private void rpcRemoteObjectDestruct(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        //First check to see if this instance exists at all.
        if (!instanceMap.containsKey(remoteObjectCall.getInstanceIdentifier()))
        {
            if (!classMap.containsKey(remoteObjectCall.getClassName()))
            {
                throw new WisperException(Error.ROUTE_NOT_FOUND, null, "No such class has been registered with this controller under route :" + remoteObjectCall.getClassName());
            } else
            {
                throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this controller under route :" + remoteObjectCall.getClassName());
            }
        }


        WisperInstanceModel wisperInstanceModel = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
        WisperClassModel wisperClassModel = classMap.get(remoteObjectCall.getClassName());
        if (!wisperInstanceModel.getInstance().getClass().getName().equals(wisperClassModel.getClassRef().getName()))
        {
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "Instance type does not match with the registered class.");
        }

        wisperInstanceModel.getInstance().destruct();
        if (remoteObjectCall.getRequest() != null)
        {
            Response response = remoteObjectCall.getRequest().createResponse();
            response.setResult(remoteObjectCall.getInstanceIdentifier());
            if (remoteObjectCall.getRequest().getResponseBlock() != null)
            {
                remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
            }
        }
        instanceMap.remove(remoteObjectCall.getInstanceIdentifier());
    }


    //=====================================================================================
    //region Method Call
    //=====================================================================================
    private void rpcRemoteObjectCallInstanceMethod(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.INSTANCE);
    }

    private void rpcRemoteObjectCallStaticMethod(RemoteObjectCall remoteObjectCall) throws WisperException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.STATIC);
    }

    private void remoteMethodCall(RemoteObjectCall remoteObjectCall, RPCRemoteObjectCallType callType) throws WisperException
    {
        WisperClassModel wisperClassModel = classMap.get(remoteObjectCall.getClassName());
        WisperInstanceModel wisperInstanceModel = instanceMap.get(remoteObjectCall.getInstanceIdentifier());

        if (wisperClassModel == null)
        {
            throw new WisperException(Error.ROUTE_NOT_FOUND, null, "No such class has been registered with this controller under route :" + remoteObjectCall.getClassName());
        }

        if (wisperInstanceModel == null && callType == RPCRemoteObjectCallType.INSTANCE)
        {
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "No such instance has been registered with this controller under route :" + remoteObjectCall.getClassName());
        }

        WisperMethod wisperMethod = null;
        if (callType == RPCRemoteObjectCallType.INSTANCE && wisperInstanceModel.getInstance().getClass().getName().equals(wisperClassModel.getClassRef().getName()))
        {
            wisperMethod = wisperClassModel.getInstanceMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(wisperMethod, wisperInstanceModel, wisperClassModel, remoteObjectCall);

        } else if (callType == RPCRemoteObjectCallType.STATIC)
        {
            wisperMethod = wisperClassModel.getStaticMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(wisperMethod, null, wisperClassModel, remoteObjectCall);
        }

    }

    private void callRpcClassMethodOnInstance(WisperMethod wisperMethod, WisperInstanceModel wisperInstance, WisperClassModel wisperClassModel, RemoteObjectCall remoteObjectCall)
            throws WisperException
    {
        if (wisperMethod == null)
        {
            String methodtype = (wisperInstance == null) ? "Static" : "Instance";
            throw new WisperException(Error.METHOD_NOT_REGISTERED, null, methodtype + "method " + remoteObjectCall.getMethodName() + " not registered on Wisper class " + remoteObjectCall.getClassName());
        }

        //If there is a call block instead of the method body there, just handle the call block and return.
        if (wisperMethod.getCallBlock() != null)
        {
            wisperMethod.getCallBlock().perform(this, wisperInstance, wisperMethod, remoteObjectCall.getRequest());
            return;
        }


        String methodName = wisperMethod.getMethodName();
        Method method;
        Object returnedValue;

        Class[] parameterTypes = wisperMethod.getParameterTypes();
        Object[] params = remoteObjectCall.getParams();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            //In case of instance, it has been replaced by RPCMethodParameter type.
            if (parameterTypes[i].equals(WisperParameterType.INSTANCE.getClass()))
            {
                if (instanceMap.containsKey(params[i].toString()))
                {
                    params[i] = instanceMap.get(params[i].toString()).getInstance();
                    parameterTypes[i] = params[i].getClass();
                }
            }
        }


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
                method = getMethod(wisperClassModel.getClassRef(), methodName, parameterTypes);
                method.setAccessible(true);
                returnedValue = method.invoke(null, params);
            }

            if (remoteObjectCall.getRequest() != null)
            {
                Response response = remoteObjectCall.getRequest().createResponse();
                if (returnedValue != null)
                {
                    response.setResult(returnedValue);
                }

                if (remoteObjectCall.getRequest().getResponseBlock() != null)
                {
                    remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
                }
            }
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Method " + methodName + " exists but is not accessible on wisper class " + remoteObjectCall.getClassName() + ". Is the method public?";
            throw new WisperException(Error.METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + remoteObjectCall.getClassName() + ". Are passed parameters of correct type?";
            throw new WisperException(Error.METHOD_INVALID_ARGUMENTS, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Method " + methodName + " could not be invoked on wisper class " + remoteObjectCall.getClassName();
            throw new WisperException(Error.METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Method " + methodName + " could not be found on class " + remoteObjectCall.getClassName() + "with argument types " + Arrays.toString(parameterTypes) + ". Is the method defined in the class?";
            throw new WisperException(Error.METHOD_NOT_FOUND, e, errorMessage);
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
