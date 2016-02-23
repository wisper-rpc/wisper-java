package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

import static com.widespace.wisper.messagetype.error.Error.UNEXPECTED_TYPE_ERROR;

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

    public void call()
    {
        WisperMethod wisperMethod;

        switch (MessageParser.getCallType(message))
        {
            case STATIC_METHOD:
            {
                wisperMethod = classModel.getInstanceMethods().get(MessageParser.getMethodName(message));
                callStatic(wisperMethod);
            }
            break;
            case INSTANCE_METHOD:
            {
                wisperMethod = classModel.getInstanceMethods().get(MessageParser.getMethodName(message));
                callInstance(wisperMethod);
            }
            break;
            default:
                break;
        }
    }

    public void callStatic(WisperMethod methodModel)
    {


    }

    public void callInstance(WisperMethod methodModel)
    {

    }
}
