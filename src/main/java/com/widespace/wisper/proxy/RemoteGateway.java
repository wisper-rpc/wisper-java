package com.widespace.wisper.proxy;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.classrepresentation.CallBlock;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.MessageFactory;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import com.widespace.wisper.route.Channel;
import com.widespace.wisper.route.ClassRouter;
import com.widespace.wisper.route.FunctionRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.MessageParser;
import com.widespace.wisper.route.Router;

import org.json.JSONObject;


/**
 * This class provides the link between the webView and the remote object controller as of sending and receiving RPC messages.
 */
public class RemoteGateway extends WisperObject
{
    private Channel channel;
    private GatewayRouter gatewayRouter;

    /**
     * Constructor
     *
     * @param channel
     */
    public RemoteGateway(Channel channel)
    {
        gatewayRouter = new GatewayRouter();
        setChannel(channel);
    }

    public RemoteGateway(GatewayRouter gatewayRouter, Channel channel)
    {
        this.gatewayRouter = gatewayRouter;
        setChannel(channel);
    }

    public static WisperClassModel registerRpcClass()
    {
        final WisperClassModel wisperClassModel = new WisperClassModel(RemoteGateway.class);

        wisperClassModel.addInstanceMethod(new WisperMethod("~", "RemoteGateway", WisperParameterType.INSTANCE));
        wisperClassModel.addInstanceMethod(new WisperMethod("setChannel", "setChannel", WisperParameterType.INSTANCE));
        wisperClassModel.addInstanceMethod(new WisperMethod("sendMessage", new SendMessageCallBlock()));
        wisperClassModel.addInstanceMethod(new WisperMethod("exposeRoute", "exposeRoute", WisperParameterType.STRING, WisperParameterType.STRING));
        wisperClassModel.addInstanceMethod(new WisperMethod("exposeReference", "exposeReference", WisperParameterType.STRING, WisperParameterType.STRING));

        return wisperClassModel;
    }

    public GatewayRouter getGatewayRouter()
    {
        return gatewayRouter;
    }

    /**
     * The channel is set remotely by the caller, i.e. from the JS side or by the ad.
     * This will enable the JS side to talk to the Gateway assigned to this WisperWebView.
     *
     * @param channel the provided channel to be assigned, i.e. WebViewChannel
     */
    public void setChannel(Channel channel)
    {
        if (this.channel != null)
            this.channel.setGateway(null);

        this.channel = channel;
        if (channel != null)
            channel.setGateway(gatewayRouter.getGateway());

        gatewayRouter.setGatewayCallback(channel);
    }


    /**
     * @param newPath   new path under which the reference is gonna be exposed
     * @param reference path to the reference we would like to expose
     */
    public void exposeReference(String newPath, String reference)
    {
        Router referenceRouter = classRouter.getRootRoute().getRouter(reference);
        if (referenceRouter instanceof ClassRouter)
        {
            Class<? extends Wisper> referenceClass = ((ClassRouter) referenceRouter).getWisperClassModel().getClassRef();
            gatewayRouter.register(referenceClass, newPath);
        }
    }


    /**
     * @param newRoute new path under which myRoute will be exposed to the gateway router of this RemoteGateway.
     * @param myRoute  path to which we will proxy incoming messages to the new route.
     */
    public void exposeRoute(final String newRoute, final String myRoute)
    {
        gatewayRouter.exposeRoute(newRoute, new ExposeRouteFunctionRouter(newRoute, myRoute));
    }

    private static class SendMessageCallBlock implements CallBlock
    {
        @Override
        public void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, final Request request) throws Exception
        {
            final String internalMessageString = (String) request.getParams()[1];
            AbstractMessage wisperMessage = new MessageFactory().createMessage(new JSONObject(internalMessageString));
            if (wisperMessage instanceof Request)
            {
                ((Request) wisperMessage).setResponseBlock(new ResponseBlock()
                {
                    @Override
                    public void perform(Response response, RPCErrorMessage error)
                    {
                        Response newResponse = new Response();
                        newResponse.setIdentifier(request.getIdentifier());
                        newResponse.setResult(response.toJsonString());
                        request.getResponseBlock().perform(newResponse, null);

                        //TODO: Handle Err case & Notifications
                    }
                });
            } else
            {
                ((RemoteGateway) wisperInstanceModel.getInstance()).gatewayRouter.getGateway().handleMessage(wisperMessage);
                Response response = request.createResponse();
                request.getResponseBlock().perform(response, null);
            }
        }
    }


    private class ExposeRouteFunctionRouter extends FunctionRouter
    {
        private final String newRoute;
        private final String myRoute;

        public ExposeRouteFunctionRouter(String newRoute, String myRoute)
        {
            this.newRoute = newRoute;
            this.myRoute = myRoute;
        }

        @Override
        public void routeMessage(final AbstractMessage message, String path) throws WisperException
        {
            if (proxiedRequestTypeMessage(message))
                return;

            proxyNotificationTypeMessage(message);

            classRouter.getRootGateway().sendMessage(message);
        }

        private void proxyNotificationTypeMessage(AbstractMessage message)
        {
            if (!(message instanceof Notification))
                return;

            String methodName = MessageParser.getFullMethodName(message);
            String replacedMethodName = methodName.replaceFirst(newRoute, myRoute);
            ((Notification) message).setMethodName(replacedMethodName);
        }

        private boolean proxiedRequestTypeMessage(final AbstractMessage message)
        {
            if (!(message instanceof Request))
                return false;

            String methodName = MessageParser.getFullMethodName(message);
            String replacedMethodName = methodName.replaceFirst(newRoute, myRoute);

            Request proxiedRequest = new Request().withMethodName(replacedMethodName).withParams(((Request) message).getParams());
            proxiedRequest.setResponseBlock(new ResponseBlock()
            {
                @Override
                public void perform(Response response, RPCErrorMessage error)
                {
                    if (error == null)
                    {
                        proxyResponse(response);
                    } else
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
            });

            return true;
        }
    }
}
