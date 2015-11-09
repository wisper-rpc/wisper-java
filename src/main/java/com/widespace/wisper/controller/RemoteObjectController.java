package com.widespace.wisper.controller;

import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.RPCEventBuilder;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.*;
import com.widespace.wisper.utils.ClassUtils;
import org.json.JSONException;

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
    HashMap<String, RPCClass> classMap;
    HashMap<String, WisperClassInstance> instanceMap;

    public RemoteObjectController(GatewayCallback callback)
    {
        super(callback);
        classMap = new HashMap<String, RPCClass>();
        instanceMap = new HashMap<String, WisperClassInstance>();
    }

    /**
     * Registers an already modeled class into the classes map
     *
     * @param clazz RPC model class
     */
    public void registerClass(RPCClass clazz)
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
    public RPCClass getRpcClassForClass(Class<?> clazz)
    {
        if (classMap != null && classMap.keySet().size() > 0)
        {
            for (String key : classMap.keySet())
            {
                RPCClass rpcClass = classMap.get(key);
                if (rpcClass.getClassRef().equals(clazz))
                {
                    return rpcClass;
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
    public WisperClassInstance getWisperClassInstance(Wisper instance)
    {
        return getWisperClassInstance(instance.toString());
    }

    public WisperClassInstance getWisperClassInstance(String instanceIdentifier)
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

    public HashMap<String, WisperClassInstance> getInstanceMap()
    {
        return instanceMap;
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
        WisperClassInstance wisperClassInstance = getWisperClassInstance(rpcInstance);

        if (wisperClassInstance == null)
        {
            return;
        }

        String instanceIdentifier = wisperClassInstance.getInstanceIdentifier();

        RPCClass rpcClass = getRpcClassForClass(rpcInstance.getClass());
        String mapName = rpcClass.getMapName();

        Event event;
        try
        {
            event = new RPCEventBuilder().withInstanceIdentifier(instanceIdentifier).withMethodName(mapName).withName(key).withValue(value).buildInstanceEvent();
            sendMessage(event);
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void handleMessage(AbstractMessage message)
    {
        super.handleMessage(message);
        RemoteObjectCall remoteObjectCall = new RemoteObjectCall(message);
        makeCall(remoteObjectCall);
    }

    // Private Methods
    private void makeCall(RemoteObjectCall remoteObjectCall)
    {
        try
        {
            switch (remoteObjectCall.getCallType())
            {
                case UNKNOWN:
                    // IGNORE
                    break;
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
                default:
                    break;
            }
        }
        catch (InvocationTargetException e)
        {
            String id = null;
            if (remoteObjectCall.getRequest() != null)
            {
                id = remoteObjectCall.getRequest().getIdentifier();
            }
            sendMessage(new RPCErrorMessageBuilder(ErrorDomain.ANDROID, -1).withMessage(e.getLocalizedMessage()).withId(id).build());
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_ARGUMENTS_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_ARGUMENTS_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_ARGUMENTS_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_ARGUMENTS_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_METHOD_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_ARGUMENTS_ERROR, e.getMessage(), remoteObjectCall);
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void handleStaticEvent(RemoteObjectCall remoteObjectCall) throws Exception
    {
        if (classMap.containsKey(remoteObjectCall.getClassName()))
        {
            RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
            String proprtyName = (String) remoteObjectCall.getParams()[0];
            String proprtyValue = (String) remoteObjectCall.getParams()[1];
            Field field = rpcClass.getClassRef().getField(proprtyName);
            field.setAccessible(true);
            field.set(null, proprtyValue);
        }
    }

    private void handleInstanceEvent(RemoteObjectCall remoteObjectCall) throws Exception
    {
        if (instanceMap.containsKey(remoteObjectCall.getInstanceIdentifier()))
        {
            WisperClassInstance wisperClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
            handlePropertySetWithInstanceEvent(wisperClassInstance, remoteObjectCall);
        }
        else
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_INSTANCE_ERROR, "No such instance found : " + remoteObjectCall.getInstanceIdentifier(), remoteObjectCall);
        }
    }

    private void handlePropertySetWithInstanceEvent(WisperClassInstance wisperClassInstance, RemoteObjectCall remoteObjectCall) throws Exception
    {

        Event event = new Event(remoteObjectCall);
        HashMap<String, RPCClassProperty> properties = wisperClassInstance.getRpcClass().getProperties();
        if (properties == null || !properties.containsKey(event.getName()))
        {
            //RPCLogger.log(RPCLogger.LogType.SDK_2_AD, "Property is not registered with the class");
            return;
        }

        RPCClassProperty property = properties.get(event.getName());
        if (property.getMode() == RPCClassPropertyMode.READ_ONLY)
        {
            //RPCLogger.log(RPCLogger.LogType.SDK_2_AD, "Insufficient access rights on property. Cannot write to the property with read only access.");
            return;
        }


        String setterMethodName = property.getSetterName();
        Wisper instance = wisperClassInstance.getInstance();

        // Instance method
        Class[] parameterTypes = RPCUtilities.convertRpcParameterTypeToClassType(property.getSetterMethodParameterType());
        // If property is pointing to an RPC instance, replace the pointer to the actual value
        if (property.getSetterMethodParameterType() == RPCMethodParameterType.INSTANCE)
        {
//            WisperClassInstance wisperClassInstance1 = getWisperClassInstance((Wisper) event.getValue());
//            parameterTypes[0] = wisperClassInstance1.getInstance().getClass();

            if (instanceMap.containsKey(event.getValue().toString()))
            {
                WisperClassInstance classInstancePointer = instanceMap.get(event.getValue().toString());
                event.setValue(classInstancePointer.getInstance());
                parameterTypes[0] = classInstancePointer.getInstance().getClass();
            }
        }
        Method method = getMethod(instance.getClass(), setterMethodName, parameterTypes);
        method.setAccessible(true);
        checkParameterTypes(parameterTypes, remoteObjectCall.getParams());
        method.invoke(instance, event.getValue());
    }

    private void createRemoteObject(RemoteObjectCall remoteObjectCall) throws ClassNotFoundException, IllegalAccessException, InstantiationException, JSONException, InvocationTargetException, NoSuchMethodException
    {
        if (classMap.containsKey(remoteObjectCall.getClassName()))
        {
            RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
            Class<?> classRef = rpcClass.getClassRef();

            Wisper instance;
            if (remoteObjectCall.getParams() != null)
            {
                Class<?> aClass = Class.forName(classRef.getName());
                Constructor<?> constructor = aClass.getConstructor(ClassUtils.getParameterClasses(remoteObjectCall.getParams()));
                instance = (Wisper) constructor.newInstance(remoteObjectCall.getParams());
            }
            else
            {
                instance = (Wisper) Class.forName(classRef.getName()).newInstance();
            }


            String nativeInstanceId = instance.toString();
            WisperClassInstance wisperClassInstance = new WisperClassInstance(rpcClass, instance, nativeInstanceId);
            instanceMap.put(nativeInstanceId, wisperClassInstance);
            instance.setRemoteObjectController(this);

            if (remoteObjectCall.getRequest() != null)
            {
                Response response = remoteObjectCall.getRequest().createResponse();
                HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
                idWithProperties.put("id", nativeInstanceId);
                idWithProperties.put("props", fetchInitializedProperties(wisperClassInstance));
                response.setResult(idWithProperties);
                remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
            }
        }
        else
        {
            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_CLASS_ERROR, "No class is registered for RPC with name " + remoteObjectCall.getClassName(), remoteObjectCall);
        }
    }

    private HashMap<String, Object> fetchInitializedProperties(WisperClassInstance wisperClassInstance) throws InvocationTargetException, IllegalAccessException
    {
        HashMap<String, Object> initializedProperties = new HashMap<String, Object>();
        HashMap<String, RPCClassProperty> properties = wisperClassInstance.getRpcClass().getProperties();
        for (String propertyName : properties.keySet())
        {
            RPCClassProperty property = properties.get(propertyName);
            Wisper instance = wisperClassInstance.getInstance();

            try
            {
                //Check if there is a getter implemented.
                Method getter = instance.getClass().getMethod(property.getSetterName().replace("set", "get"));
                Object value = getter.invoke(instance);

                //Check if the value is something other than null (default)
                if (value == null)
                {
                    continue;
                }

                if (property.getSetterMethodParameterType() == RPCMethodParameterType.INSTANCE)
                {
                    WisperClassInstance WisperClassInstance = getWisperClassInstance((Wisper) value);
                    value = WisperClassInstance.getInstanceIdentifier();
                }

                initializedProperties.put(propertyName, value);
            }
            catch (NoSuchMethodException e)
            {
                //Do nothing. The Getter has just not been implemented.
            }
        }

        return initializedProperties;
    }

    private void rpcRemoteObjectDestruct(RemoteObjectCall remoteObjectCall) throws JSONException
    {
        if (!instanceMap.containsKey(remoteObjectCall.getInstanceIdentifier()))
        {
            if (!classMap.containsKey(remoteObjectCall.getClassName()))
            {
                handleRemoteObjectError(RemoteObjectErrorCode.MISSING_CLASS_ERROR, "No such class registered : " + remoteObjectCall.getClassName(), remoteObjectCall);
            }
            else
            {
                handleRemoteObjectError(RemoteObjectErrorCode.INVALID_INSTANCE_ERROR, "No such instance found : " + remoteObjectCall.getInstanceIdentifier(), remoteObjectCall);
            }
            return;
        }

        WisperClassInstance wisperClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
        RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
        if (!wisperClassInstance.getInstance().getClass().getName().equals(rpcClass.getClassRef().getName()))
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_INSTANCE_ERROR, "No such instance was found with identifier " + remoteObjectCall.getInstanceIdentifier()
                    + "of type :" + remoteObjectCall.getClassName(), remoteObjectCall);
        }

        instanceMap.remove(remoteObjectCall.getInstanceIdentifier());
        wisperClassInstance.getInstance().destruct();
        if (remoteObjectCall.getRequest() != null)
        {
            Response response = remoteObjectCall.getRequest().createResponse();
            response.setResult(remoteObjectCall.getInstanceIdentifier());
            remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
        }
    }

    private void rpcRemoteObjectCallInstanceMethod(RemoteObjectCall remoteObjectCall) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.INSTANCE);
    }

    private void rpcRemoteObjectCallStaticMethod(RemoteObjectCall remoteObjectCall) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.STATIC);
    }

    private void remoteMethodCall(RemoteObjectCall remoteObjectCall, RPCRemoteObjectCallType callType) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
        WisperClassInstance wisperClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());

        RPCClassMethod theMethod = null;
        if (callType == RPCRemoteObjectCallType.INSTANCE && wisperClassInstance != null && wisperClassInstance.getInstance().getClass().getName().equals(rpcClass.getClassRef().getName()))
        {
            theMethod = rpcClass.getInstanceMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(theMethod, wisperClassInstance, rpcClass, remoteObjectCall);

        }
        else if (callType == RPCRemoteObjectCallType.STATIC && rpcClass != null)
        {
            theMethod = rpcClass.getStaticMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(theMethod, null, rpcClass, remoteObjectCall);
        }

        if (theMethod == null)
        {
            if (wisperClassInstance == null)
            {
                handleRpcError(RPCErrorCodes.MISSING_PROCEDURE_ERROR, "No such method found with name " + remoteObjectCall.getMethodName() + " on RPC object named " + remoteObjectCall.getClassName(), remoteObjectCall);
                return;
            }

            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_METHOD_ERROR, "No such method found with name " + remoteObjectCall.getMethodName(), remoteObjectCall);
        }

    }

    private void handleRemoteObjectError(RemoteObjectErrorCode errorCode, String message, RemoteObjectCall remoteObjectCall)
    {
        String Identifier = null;
        if (remoteObjectCall != null && remoteObjectCall.getRequest() != null)
        {
            Identifier = remoteObjectCall.getRequest().getIdentifier();
        }
        RPCErrorMessage error = new RPCErrorMessageBuilder(ErrorDomain.REMOTE_OBJECT, errorCode.getErrorCode()).withMessage(message).withName(errorCode.getErrorName()).withId(Identifier).build();
        sendMessage(error);
    }

    private void handleRpcError(RPCErrorCodes rpcErrorCode, String message, RemoteObjectCall remoteObjectCall)
    {
        String Identifier = null;
        if (remoteObjectCall != null && remoteObjectCall.getRequest() != null)
        {
            Identifier = remoteObjectCall.getRequest().getIdentifier();
        }
        RPCErrorMessage errorMessage = new RPCErrorMessageBuilder(ErrorDomain.RPC, rpcErrorCode.getErrorCode()).withMessage(message).withName(rpcErrorCode.getErrorName()).withId(Identifier).build();
        sendMessage(errorMessage);
    }

    private void callRpcClassMethodOnInstance(RPCClassMethod rpcClassMethod, WisperClassInstance rpcInstance, RPCClass rpcClass, RemoteObjectCall remoteObjectCall)
            throws InvocationTargetException, IllegalAccessException, JSONException, NoSuchMethodException
    {
        if (rpcClassMethod == null)
        {
            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_METHOD_ERROR, "No such method found with name " + remoteObjectCall.getMethodName(), remoteObjectCall);
            return;
        }

        if (rpcClassMethod.getCallBlock() != null)
        {
            rpcClassMethod.getCallBlock().perform(this, rpcInstance, rpcClassMethod, remoteObjectCall.getRequest());
            return;
        }

        String methodName = rpcClassMethod.getMethodName();
        Method method;
        Object returnedValue;

        Class[] parameterTypes = rpcClassMethod.getParameterTypes();
        Object[] params = remoteObjectCall.getParams();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            //In case of instance, it has been replaced by RPCMethodParameter type.
            if (parameterTypes[i].equals(RPCMethodParameterType.INSTANCE.getClass()))
            {
                if (instanceMap.containsKey(params[i].toString()))
                {
                    params[i] = instanceMap.get(params[i].toString()).getInstance();
                    parameterTypes[i] = params[i].getClass();
                }
            }
        }


        if (rpcInstance != null)
        {
            Wisper instance = rpcInstance.getInstance();

            // Instance method
            method = getMethod(instance.getClass(), methodName, parameterTypes);
            method.setAccessible(true);
            checkParameterTypes(parameterTypes, params);
            returnedValue = method.invoke(instance, params);
        }
        else
        {
            // Static method
            method = getMethod(rpcClass.getClassRef(), methodName, parameterTypes);
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
            remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
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

        throw new NoSuchMethodException("No such method found with name " + methodName + " and parameter types " + Arrays.toString(parameterTypes));
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


    public WisperClassInstance addRpcObjectInstance(Wisper rpcObjectInstance, RPCClass rpcClass)
    {
        rpcObjectInstance.setRemoteObjectController(this);
        String key = rpcObjectInstance.toString();
        WisperClassInstance wisperClassInstance = new WisperClassInstance(rpcClass, rpcObjectInstance, key);
        instanceMap.put(key, wisperClassInstance);
        return wisperClassInstance;
    }

    public boolean removeRpcObjectInstance(WisperClassInstance rpcObjectInstance)
    {
        if (instanceMap.containsValue(rpcObjectInstance))
        {
            rpcObjectInstance.getInstance().setRemoteObjectController(null);
            instanceMap.remove(rpcObjectInstance);
            return true;
        }

        return false;
    }

}
