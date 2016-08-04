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
import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.messagetype.MessageFactory;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.route.Channel;
import com.widespace.wisper.route.ClassRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.ProxyFunctionRouter;
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
        wisperClassModel.addInstanceMethod(new WisperMethod("exposeRoute", "exposeRoute", WisperParameterType.STRING, WisperParameterType.STRING, WisperParameterType.BOOLEAN));
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
        {
            this.channel.setGateway(null);
        }

        this.channel = channel;
        if (channel != null)
        {
            channel.setGateway(gatewayRouter.getGateway());
        }

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
     * @param reverseRoute
     */
    public void exposeRoute(final String newRoute, final String myRoute, final Boolean reverseRoute)
    {
        gatewayRouter.exposeRoute(newRoute, new ProxyFunctionRouter(newRoute, myRoute, (GatewayRouter) classRouter.getRootRoute()));

        if(reverseRoute)
        {
            ((GatewayRouter)classRouter.getRootRoute()).exposeRoute(myRoute, new ProxyFunctionRouter(myRoute, newRoute, gatewayRouter));
        }
    }

    private static class SendMessageCallBlock implements CallBlock
    {
        @Override
        public void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, final CallMessage message) throws Exception
        {
            if (message instanceof Request)
            {
                final Request request = (Request) message;
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

                            //TODO: Handle Err case & Notifications?
                        }
                    });
                }
                else
                {
                    ((RemoteGateway) wisperInstanceModel.getInstance()).gatewayRouter.getGateway().handleMessage(wisperMessage);
                    Response response = request.createResponse();
                    request.getResponseBlock().perform(response, null);
                }
            }
            else if (message instanceof Notification)
            {
                final Notification notification = (Notification) message;
                final String internalMessageString = (String) notification.getParams()[1];
                AbstractMessage wisperMessage = new MessageFactory().createMessage(new JSONObject(internalMessageString));
                if (wisperMessage instanceof Request)
                {
                    ((Request) wisperMessage).setResponseBlock(new ResponseBlock()
                    {
                        @Override
                        public void perform(Response response, RPCErrorMessage error)
                        {
                            Response newResponse = new Response();
                            newResponse.setIdentifier(notification.getIdentifier());
                            newResponse.setResult(response.toJsonString());
                            //notification.getResponseBlock().perform(newResponse, null);

                            //TODO: Handle Err case & Notifications?
                        }
                    });
                }
                else
                {
                    ((RemoteGateway) wisperInstanceModel.getInstance()).gatewayRouter.getGateway().sendMessage(wisperMessage);
                    //Response response = notification.createResponse();
                    //notification.getResponseBlock().perform(response, null);
                }
            }
        }
    }
}
