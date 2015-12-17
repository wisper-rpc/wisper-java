package com.widespace.wisper.messagetype.error;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.RemoteObjectCall;

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
    private RemoteObjectCall remoteObjectCall;


    public WisperExceptionHandler(Gateway gateway, RemoteObjectCall remoteObjectCall)
    {
        this.gateway = gateway;
        this.remoteObjectCall = remoteObjectCall;
    }


    public void handle(WisperException ex)
    {
        StackTraceElement[] stackTrace = ex.getUnderlyingException().getStackTrace();

        RPCError underlying = new RPCError();
        underlying.setCode(CODE_CANNOT_BE_DETERMINED);
        underlying.setDomain(1);
        underlying.setMessage(Arrays.toString(stackTrace));
        underlying.setUnderlyingError(null);

        RPCErrorMessage errorMessage = new RPCErrorMessageBuilder(ErrorDomain.NATIVE, ex.getErrorCode())
                .withMessage(ex.getMessage())
                .withName(ex.getError().name())
                .withUnderlyingError(underlying)
                .build();

        if (remoteObjectCall.getRequest() != null)
        {
            errorMessage.setId(remoteObjectCall.getRequest().getIdentifier());
            if (remoteObjectCall.getRequest().getResponseBlock() != null)
                remoteObjectCall.getRequest().getResponseBlock().perform(null, errorMessage);
            return;
        }

        gateway.sendMessage(errorMessage);

        //TODO: REMOVE FROM PRODUCTION
        System.out.println(errorMessage);
    }

}
