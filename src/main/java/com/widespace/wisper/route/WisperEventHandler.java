package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.utils.RPCUtilities;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;

public class WisperEventHandler
{
    private Router router;
    private WisperClassModel wisperClassModel;
    private AbstractMessage message;

    public WisperEventHandler(@NotNull Router router, @NotNull WisperClassModel wisperClassModel, @NotNull AbstractMessage message)
    {
        this.router = router;
        this.wisperClassModel = wisperClassModel;
        this.message = message;
    }

    public void handle() throws WisperException
    {
        WisperCallType callType = MessageParser.getCallType(message);
        if (! ((message instanceof Notification) && (callType == WisperCallType.STATIC_EVENT || callType == WisperCallType.INSTANCE_EVENT) ))
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance event handler was called with a non-Notification/Event message type.");

        Event event = new Event((Notification)message);
        message = null;
        handle(event, callType);
    }

    private void handle(Event eventMessage, WisperCallType callType)
    {
        switch (callType)
        {
            case STATIC_EVENT:
                handleStaticEvent(eventMessage);
                break;
            case INSTANCE_EVENT:
                handleInstacneEvent(eventMessage);
                break;
            default:
                break;
        }
    }

    //------------------------------------------------------------------------
    //region Static Events
    //------------------------------------------------------------------------
    private void handleStaticEvent(Event eventMessage)
    {
        try
        {
            handledStaticPropertySet(eventMessage);
        } catch (WisperException ex)
        {
            //Swallow
            System.out.println("WisperEventHandler : Wisper Exception swallowed : " + ex.toString());
        } finally
        {
            callStaticEventHandlerOnClass(eventMessage);
        }
    }

    private boolean handledStaticPropertySet(Event eventMessage) throws WisperException
    {
        boolean result = false;
        String proprtyName = null;
        try
        {
            Object[] params = MessageParser.getParams(eventMessage);
            proprtyName = (String) params[0];
            String proprtyValue = (String) params[1];
            Field field = wisperClassModel.getClassRef().getField(proprtyName);
            field.setAccessible(true);
            field.set(null, proprtyValue);
            result = true;

        } catch (NoSuchFieldException e)
        {
            String errorMessage = "No such field defined as " + proprtyName + ".Is the property registered with the class model?";
            throw new WisperException(Error.PROPERTY_NOT_REGISTERED, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Property " + proprtyName + " is not accessible in class " + wisperClassModel.getClassRef() + ". Is the property public?";
            throw new WisperException(Error.PROPERTY_NOT_ACCESSIBLE, e, errorMessage);
        } catch (NullPointerException npe)
        {
            if (proprtyName == null)
            {
                String errorMessage = "Property name or value does not exist in the event notification. " + this.message.toJsonString();
                throw new WisperException(Error.FORMAT_ERROR, npe, errorMessage);
            }
        }
        return result;
    }

    private void callStaticEventHandlerOnClass(Event eventMessage)
    {
        try
        {
            Class<?> classRef = wisperClassModel.getClassRef();
            Method wisperStaticEventHandlerMethod = classRef.getMethod("wisperStaticEventHandler", Event.class);
            wisperStaticEventHandlerMethod.invoke(null, eventMessage);
        } catch (Exception e)
        {
            System.out.println("WisperEventHandler : Exception swallowed : " + e.toString());
        }
    }

    //------------------------------------------------------------------------
    //region Instance Events
    //------------------------------------------------------------------------
    private void handleInstacneEvent(Event eventMessage)
    {
        String instanceIdentifier = eventMessage.getInstanceIdentifier();
        WisperInstanceModel wisperInstanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier);
        if (wisperInstanceModel == null)
        {
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "Instance with ID " + instanceIdentifier + "not found in instance registry. Make sure the instance is registered and it comes with the message.");
        }

        try
        {
            handleInstancePropertySet(wisperInstanceModel, eventMessage);
        } catch (WisperException ex)
        {
            //Swallow
            System.out.println("WisperEventHandler : Wisper Exception swallowed : " + ex.toString());
        } finally
        {
            callInstanceEventHandlerOnClass(wisperInstanceModel, eventMessage);
        }
    }

    private void handleInstancePropertySet(WisperInstanceModel instanceModel, Event eventMessage) throws WisperException
    {
        String eventPropertyName = eventMessage.getName();
        if (wisperClassModel.getProperties() == null || !wisperClassModel.getProperties().containsKey(eventPropertyName))
        {
            String errorMessage = "No such field defined as " + eventPropertyName + " in class " + wisperClassModel.getClassRef() + ".Is the property registered with the class model?";
            throw new WisperException(Error.PROPERTY_NOT_REGISTERED, null, errorMessage);
        }

        WisperProperty property = wisperClassModel.getProperties().get(eventPropertyName);
        if (property.getMode() == WisperPropertyAccess.READ_ONLY)
        {
            throw new WisperException(Error.PROPERTY_NOT_ACCESSIBLE, null, "Property " + eventPropertyName + "is readonly and cannot be set.");
        }

        String setterMethodName = property.getSetterName();
        Wisper instance = instanceModel.getInstance();

        // Instance method
        Class[] parameterTypes = RPCUtilities.convertRpcParameterTypeToClassType(property.getSetterMethodParameterType());

        // Extract the value for use when invoking the setter method
        Object value = eventMessage.getValue();

        // If property is pointing to an RPC instance, replace the pointer to the actual value
        if (property.getSetterMethodParameterType() == WisperParameterType.INSTANCE)
        {
            WisperInstanceModel paramInstanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId((String) value);
            if (paramInstanceModel != null)
            {
                // Replace the value, if we find an instance for the given reference
                value = paramInstanceModel.getInstance();
                parameterTypes[0] = value.getClass();
            }
        }

        try
        {
            Method method = getMethod(instance.getClass(), setterMethodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(instance, value);

        } catch (NoSuchMethodException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "not found in class " + wisperClassModel.getClassRef() + ". Does the setter method actually exist in the class? ";
            throw new WisperException(Error.SETTER_METHOD_NOT_FOUND, e, errorMessage);
        } catch (IllegalAccessException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "was not accessible in class  " + wisperClassModel.getClassRef() + ".Is the setter method public?";
            throw new WisperException(Error.SETTER_METHOD_NOT_ACCESSIBLE, e, errorMessage);
        } catch (InvocationTargetException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "could not be invoked in class  " + wisperClassModel.getClassRef();
            throw new WisperException(Error.SETTER_METHOD_INVOCATION_ERROR, e, errorMessage);
        } catch (IllegalArgumentException e)
        {
            String errorMessage = "Setter method for the property " + property.getMappingName() + "rejected the argument sent in class  " + wisperClassModel.getClassRef() + "Are the arguments passed correctly?";
            throw new WisperException(Error.SETTER_METHOD_WRONG_ARGUMENTS, e, errorMessage);
        }
    }

    private void callInstanceEventHandlerOnClass(WisperInstanceModel instanceModel, Event eventMessage)
    {
        try
        {
            Wisper instance = instanceModel.getInstance();
            Class<?> classRef = wisperClassModel.getClassRef();
            Method wisperStaticEventHandlerMethod = classRef.getMethod("wisperEventHandler", Event.class);
            wisperStaticEventHandlerMethod.invoke(instance, eventMessage);
        } catch (Exception e)
        {
            System.out.println("WisperEventHandler : Exception swallowed : " + e.toString());
        }
    }


    //------------------------------------------------------------------------
    //region Reflection and utility
    //------------------------------------------------------------------------

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

}
