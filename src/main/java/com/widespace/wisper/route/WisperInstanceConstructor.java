package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.classrepresentation.WisperProperty;
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
import java.util.HashMap;

import static com.widespace.wisper.messagetype.error.Error.NOT_ALLOWED;
import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;

/**
 * This class specifically tries to create an instance of a remote object using the message.
 */
public class WisperInstanceConstructor
{
    private final WisperClassModel classModel;
    private final Request request;

    public WisperInstanceConstructor(@NotNull WisperClassModel classModel, @NotNull AbstractMessage message)
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
            if (handleBlockConstructor(callback))
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


    private HashMap<String, Object> fetchInitializedProperties(WisperInstanceModel wisperInstanceModel)
    {
        HashMap<String, Object> initializedProperties = new HashMap<String, Object>();
        HashMap<String, WisperProperty> properties = classModel.getProperties();
        String currentPropertyName = null;
        try
        {
            for (String propertyName : properties.keySet())
            {
                currentPropertyName = propertyName;

                WisperProperty property = properties.get(propertyName);
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
                    WisperInstanceModel WisperInstanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId((String) value);
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
}

