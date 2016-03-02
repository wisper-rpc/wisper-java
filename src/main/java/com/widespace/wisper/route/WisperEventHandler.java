package com.widespace.wisper.route;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
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
        handle(message);
    }

    private void handle(AbstractMessage message)
    {
        if (!(message instanceof Event))
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance event handler was called with a non-Notification/Event message type.");

        WisperCallType callType = MessageParser.getCallType(message);
        if (callType != WisperCallType.STATIC_EVENT && callType != WisperCallType.INSTANCE_EVENT)
            throw new WisperException(UNEXPECTED_TYPE_ERROR, null, "Remote instance event handler was called with a non-Event message call type.");

        switch (callType)
        {
            case STATIC_EVENT:
                handleStaticEvent(message);
                break;
            case INSTANCE_EVENT:
                handleInstacneEvent(message);
                break;
            default:
                break;
        }
    }

    private void handleInstacneEvent(AbstractMessage message)
    {
        String instanceIdentifier = MessageParser.getInstanceIdentifier(message);
        WisperInstanceModel wisperInstanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId(instanceIdentifier);
        if (wisperInstanceModel == null)
        {
            throw new WisperException(Error.WISPER_INSTANCE_INVALID, null, "Instance with ID " + instanceIdentifier + "not found in instance registry. Make sure the instance is registered and it comes with the message.");
        }
    }

    private void handleStaticEvent(AbstractMessage message)
    {
        try
        {
            handledStaticPropertySet(message);
        } catch (WisperException ex)
        {
            //Swallow
            System.out.println("WisperEventHandler : Wisper Exception swallowed : " + ex.toString());
        } finally
        {
            callStaticEventHandlerOnClass(message);
        }
    }

    private void callStaticEventHandlerOnClass(AbstractMessage eventMessage)
    {
        try
        {
            Event event = (Event) eventMessage;
            Class<?> classRef = wisperClassModel.getClassRef();
            Method wisperStaticEventHandlerMethod = classRef.getMethod("wisperStaticEventHandler", Event.class);
            wisperStaticEventHandlerMethod.invoke(null, event);
        } catch (Exception e)
        {
            System.out.println("WisperEventHandler : Exception swallowed : " + e.toString());
        }
    }

    private boolean handledStaticPropertySet(AbstractMessage message) throws WisperException
    {
        boolean result = false;
        String proprtyName = null;
        try
        {
            Object[] params = MessageParser.getParams(this.message);
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

}
