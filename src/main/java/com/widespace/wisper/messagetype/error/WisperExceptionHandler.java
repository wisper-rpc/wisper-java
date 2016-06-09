package com.widespace.wisper.messagetype.error;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * WisperExceptionHandler handles exceptions of type WisperException by logging correct messsages and
 * sending back the relevant Wisper error to the Gateway.
 *
 * @see com.widespace.wisper.controller.Gateway
 * @see WisperException
 */
public class WisperExceptionHandler
{
    public static final int CODE_CANNOT_BE_DETERMINED = -2;
    private final Gateway gateway;


    public WisperExceptionHandler(@NotNull Gateway gateway)
    {
        this.gateway = gateway;
    }


    public void handle(@NotNull WisperException ex, @Nullable AbstractMessage message)
    {
        System.err.println("Handling Exception Thrown by Wisper: ");
        ex.printStackTrace();

        RPCErrorMessage errorMessage = new RPCErrorMessageBuilder(ErrorDomain.NATIVE, ex.getErrorCode())
                .withMessage(ex.getMessage())
                .withName(ex.getError().name())
                .withUnderlyingError(getUnderlyingError(ex))
                .build();


        if (message != null && message instanceof Request)
        {
            respondTheRequestWithError((Request) message, errorMessage);
            return;
        }

        gateway.sendMessage(errorMessage);
    }

    private RPCError getUnderlyingError(WisperException ex)
    {
        if (ex.getUnderlyingException() == null)
            return null;

        StackTraceElement[] stackTrace = ex.getUnderlyingException().getStackTrace();

        RPCError underlying = new RPCError();
        underlying.setCode(CODE_CANNOT_BE_DETERMINED);
        underlying.setDomain(1);
        underlying.setMessage(Arrays.toString(stackTrace));
        underlying.setUnderlyingError(null);

        return underlying;
    }

    private void respondTheRequestWithError(Request message, RPCErrorMessage errorMessage)
    {
        errorMessage.setId(message.getIdentifier());
        if (message.getResponseBlock() != null)
        {
            message.getResponseBlock().perform(null, errorMessage);
        }
    }

}
