package com.widespace.wisper.route;

import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;

/**
 * Created by rana on 8/3/16.
 */
public class ProxyFunctionRouter extends FunctionRouter
{
    private String newRoute;
    private String myRoute;
    private GatewayRouter gatewayRouter;

    public ProxyFunctionRouter(String newPath, String myPath, GatewayRouter gatewayRouter)
    {
        this.newRoute = newPath;
        this.myRoute = myPath;
        this.gatewayRouter = gatewayRouter;
    }

    @Override
    public void routeMessage(CallMessage message, String path) throws WisperException
    {
        if (proxiedRequestTypeMessage(message))
        {
            return;
        }

        proxyNotificationTypeMessage(message);

    }

    private void proxyNotificationTypeMessage(AbstractMessage message)
    {
        if (!(message instanceof Notification))
        {
            return;
        }

        String methodName = MessageParser.getFullMethodName(message);
        String replacedMethodName = methodName.replaceFirst(newRoute, myRoute);

        gatewayRouter.getGateway().sendMessage(new Notification(replacedMethodName, ((Notification) message).getParams()));
    }

    private boolean proxiedRequestTypeMessage(final AbstractMessage message)
    {
        if (!(message instanceof Request))
        {
            return false;
        }

        String methodName = MessageParser.getFullMethodName(message);
        String replacedMethodName = methodName.replaceFirst(newRoute, myRoute);

        ResponseBlock block = new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                if (error == null)
                {
                    proxyResponse(response);
                }
                else
                {
                    proxyError(error);
                }
            }

            private void proxyError(RPCErrorMessage error)
            {
                RPCErrorMessage proxiedError = new RPCErrorMessage(error.getId(), error.getError());
                ((Request) message).getResponseBlock().perform(null, proxiedError);
            }

            private void proxyResponse(Response response)
            {
                Response proxiedResponse = new Response();
                proxiedResponse.setIdentifier(((Request) message).getIdentifier());
                proxiedResponse.setResult(response.getResult());

                ((Request) message).getResponseBlock().perform(proxiedResponse, null);
            }
        };

        Request proxiedRequest = new Request(replacedMethodName, ((Request) message).getParams()).withResponseBlock(block);

        gatewayRouter.getGateway().sendMessage(proxiedRequest);

        return true;
    }
}
