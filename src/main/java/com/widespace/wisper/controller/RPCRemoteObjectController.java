package com.widespace.wisper.controller;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.*;
import com.widespace.wisper.messagetype.error.RPCError;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * Subclass of the Gateway to extend functionality for handling instances
 * with rpc messages. This class will allow you to register classes to be used
 * by the rpc bridge through exposed methods.
 * <p/>
 * Created by Ehssan Hoorvash on 22/05/14.
 */
public class RPCRemoteObjectController extends Gateway
{
    HashMap<String, RPCClass> classMap;
    HashMap<String, RPCClassInstance> instanceMap;

    public RPCRemoteObjectController(GatewayCallback callback)
    {
        super(callback);
        classMap = new HashMap<String, RPCClass>();
        instanceMap = new HashMap<String, RPCClassInstance>();
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
    public RPCClassInstance getRpcClassInstance(Wisper instance)
    {
        return instanceMap.get(instance.toString());
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

    public HashMap<String, RPCClassInstance> getInstanceMap()
    {
        return instanceMap;
    }

    /**
     * Sends a wisper event to an instance.
     *
     * @param rpcInstance the instance to which the event is sent.
     * @param key event key.
     * @param value the value wrapped in the event.
     */
    public void sendInstanceEvent(Wisper rpcInstance, String key, Object value)
    {
        RPCClassInstance rpcClassInstance = getRpcClassInstance(rpcInstance);

        if (rpcClassInstance == null)
        {
            return;
        }

        String instanceIdentifier = rpcClassInstance.getInstanceIdentifier();

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
        RPCRemoteObjectCall remoteObjectCall = new RPCRemoteObjectCall(message);
        makeCall(remoteObjectCall);
    }

    // Private Methods
    private void makeCall(RPCRemoteObjectCall remoteObjectCall)
    {
        try
        {
            switch (remoteObjectCall.getCallType())
            {
                case UNKNOWN:
                    // IGNORE
                    break;
                case CREATE:
                    rpcRemoteObjectConstruct(remoteObjectCall);
                    break;
                case DESTROY:
                    rpcRemoteObjectDestruct(remoteObjectCall);
                    break;
                case STATIC:
                    rpcRemoteObjectCallStaticMethod(remoteObjectCall);
                    break;
                case STATIC_EVENT:
                    // TODO: IMPLEMENT
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
            if (remoteObjectCall.getRequest()!=null)
            {
                id =remoteObjectCall.getRequest().getIdentifier();
            }
            sendMessage(new RPCErrorBuilder(ErrorDomain.ANDROID, -1).withMessage(e.getLocalizedMessage()).withId(id).build());
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

    private void handleStaticEvent(RPCRemoteObjectCall remoteObjectCall) throws Exception
    {
        if(classMap.containsKey(remoteObjectCall.getClassName()))
        {
            RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
            String proprtyName = (String) remoteObjectCall.getParams()[0];
            String proprtyValue = (String) remoteObjectCall.getParams()[1];
            Field field = rpcClass.getClassRef().getDeclaredField(proprtyName);
            field.set(null, proprtyValue);
        }
    }

    private void handleInstanceEvent(RPCRemoteObjectCall remoteObjectCall) throws Exception
    {
        if (instanceMap.containsKey(remoteObjectCall.getInstanceIdentifier()))
        {
            RPCClassInstance rpcClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
            handlePropertySetWithInstanceEvent(rpcClassInstance, remoteObjectCall);
        }
        else
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_INSTANCE_ERROR, "No such instance found : " + remoteObjectCall.getInstanceIdentifier(), remoteObjectCall);
        }
    }

    private void handlePropertySetWithInstanceEvent(RPCClassInstance rpcClassInstance, RPCRemoteObjectCall remoteObjectCall) throws Exception
    {

        Event event = new Event(remoteObjectCall);
        HashMap<String, RPCClassProperty> properties = rpcClassInstance.getRpcClass().getProperties();
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
        Wisper instance = rpcClassInstance.getInstance();

        // Instance method
        Class[] parameterTypes = RPCUtilities.convertRpcParameterTypeToClassType(property.getSetterMethodParameterType());
        // If property is pointing to an RPC instance, replace the pointer to the actual value
        if (property.getSetterMethodParameterType() == RPCMethodParameterType.INSTANCE)
        {
            if (instanceMap.containsKey(event.getValue().toString()))
            {
                RPCClassInstance classInstancePointer = instanceMap.get(event.getValue().toString());
                event.setValue(classInstancePointer.getInstance());
                parameterTypes[0] = Wisper.class;
            }
        }
        Method method = instance.getClass().getMethod(setterMethodName, parameterTypes);
        checkParameterTypes(parameterTypes, remoteObjectCall.getParams());
        method.invoke(instance, event.getValue());
    }

    private void rpcRemoteObjectConstruct(RPCRemoteObjectCall remoteObjectCall) throws ClassNotFoundException, IllegalAccessException, InstantiationException, JSONException
    {
        if (classMap.containsKey(remoteObjectCall.getClassName()))
        {
            RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
            Class<?> classRef = rpcClass.getClassRef();
            Wisper instance = (Wisper) Class.forName(classRef.getName()).newInstance();

            String key = instance.toString();
            RPCClassInstance rpcClassInstance = new RPCClassInstance(rpcClass, instance, key);
            instanceMap.put(key, rpcClassInstance);
            instance.setRemoteObjectController(this);

            if (remoteObjectCall.getRequest() != null)
            {
                Response response = remoteObjectCall.getRequest().createResponse();
                response.setResult(key);
                remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
            }
        }
        else
        {
            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_CLASS_ERROR, "No class is registered for RPC with name " + remoteObjectCall.getClassName(), remoteObjectCall);
        }
    }

    private void rpcRemoteObjectDestruct(RPCRemoteObjectCall remoteObjectCall) throws JSONException
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

        RPCClassInstance rpcClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());
        RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
        if (!rpcClassInstance.getInstance().getClass().getName().equals(rpcClass.getClassRef().getName()))
        {
            handleRemoteObjectError(RemoteObjectErrorCode.INVALID_INSTANCE_ERROR, "No such instance was found with identifier " + remoteObjectCall.getInstanceIdentifier()
                    + "of type :" + remoteObjectCall.getClassName(), remoteObjectCall);
        }

        instanceMap.remove(remoteObjectCall.getInstanceIdentifier());
        rpcClassInstance.getInstance().destruct();
        if (remoteObjectCall.getRequest() != null)
        {
            Response response = remoteObjectCall.getRequest().createResponse();
            response.setResult(remoteObjectCall.getInstanceIdentifier());
            remoteObjectCall.getRequest().getResponseBlock().perform(response, null);
        }
    }

    private void rpcRemoteObjectCallInstanceMethod(RPCRemoteObjectCall remoteObjectCall) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.INSTANCE);
    }

    private void rpcRemoteObjectCallStaticMethod(RPCRemoteObjectCall remoteObjectCall) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        remoteMethodCall(remoteObjectCall, RPCRemoteObjectCallType.STATIC);
    }

    private void remoteMethodCall(RPCRemoteObjectCall remoteObjectCall, RPCRemoteObjectCallType callType) throws InvocationTargetException, IllegalAccessException, JSONException,
            NoSuchMethodException
    {
        RPCClass rpcClass = classMap.get(remoteObjectCall.getClassName());
        RPCClassInstance rpcClassInstance = instanceMap.get(remoteObjectCall.getInstanceIdentifier());

        RPCClassMethod theMethod = null;
        if (callType == RPCRemoteObjectCallType.INSTANCE && rpcClassInstance != null && rpcClassInstance.getInstance().getClass().getName().equals(rpcClass.getClassRef().getName()))
        {
            theMethod = rpcClass.getInstanceMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(theMethod, rpcClassInstance, rpcClass, remoteObjectCall);

        }
        else if (callType == RPCRemoteObjectCallType.STATIC && rpcClass != null)
        {
            theMethod = rpcClass.getStaticMethods().get(remoteObjectCall.getMethodName());
            callRpcClassMethodOnInstance(theMethod, null, rpcClass, remoteObjectCall);
        }

        if (theMethod == null)
        {
            if (rpcClassInstance == null)
            {
                handleRpcError(RPCErrorCodes.MISSING_PROCEDURE_ERROR, "No such method found with name " + remoteObjectCall.getMethodName() + " on RPC object named " + remoteObjectCall.getClassName(), remoteObjectCall);
                return;
            }

            handleRemoteObjectError(RemoteObjectErrorCode.MISSING_METHOD_ERROR, "No such method found with name " + remoteObjectCall.getMethodName(), remoteObjectCall);
        }

    }

    private void handleRemoteObjectError(RemoteObjectErrorCode errorCode, String message, RPCRemoteObjectCall remoteObjectCall)
    {
        String Identifier = null;
        if (remoteObjectCall != null && remoteObjectCall.getRequest() != null)
        {
            Identifier = remoteObjectCall.getRequest().getIdentifier();
        }
        RPCError RPCError = new RPCErrorBuilder(ErrorDomain.REMOTE_OBJECT, errorCode.getErrorCode()).withMessage(message).withName(errorCode.getErrorName()).withId(Identifier).build();
        sendMessage(RPCError);
    }

    private void handleRpcError(RPCErrorCodes rpcErrorCode, String message, RPCRemoteObjectCall remoteObjectCall)
    {
        String Identifier = null;
        if (remoteObjectCall != null && remoteObjectCall.getRequest() != null)
        {
            Identifier = remoteObjectCall.getRequest().getIdentifier();
        }
        RPCError RPCError = new RPCErrorBuilder(ErrorDomain.RPC, rpcErrorCode.getErrorCode()).withMessage(message).withName(rpcErrorCode.getErrorName()).withId(Identifier).build();
        sendMessage(RPCError);
    }

    private void callRpcClassMethodOnInstance(RPCClassMethod rpcClassMethod, RPCClassInstance rpcInstance, RPCClass rpcClass, RPCRemoteObjectCall remoteObjectCall)
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
            method = instance.getClass().getMethod(methodName, parameterTypes);
            checkParameterTypes(parameterTypes, params);
            returnedValue = method.invoke(instance, params);
        }
        else
        {
            // Static method
            method = rpcClass.getClassRef().getMethod(methodName, parameterTypes);
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


    public RPCClassInstance addRpcObjectInstance(Wisper rpcObjectInstance, RPCClass rpcClass)
    {
        rpcObjectInstance.setRemoteObjectController(this);
        String key = rpcObjectInstance.toString();
        RPCClassInstance rpcClassInstance = new RPCClassInstance(rpcClass, rpcObjectInstance, key);
        instanceMap.put(key, rpcClassInstance);
        return rpcClassInstance;
    }

    public boolean removeRpcObjectInstance(RPCClassInstance rpcObjectInstance)
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
