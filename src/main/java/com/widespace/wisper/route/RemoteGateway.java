package com.widespace.wisper.route;

import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.classrepresentation.RPCClass;
import com.widespace.wisper.classrepresentation.RPCClassMethod;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.controller.RemoteObjectController;
import com.widespace.wisper.messagetype.AbstractMessage;


/**
 * This class provides the link between the webView and the remote object controller as of sending and receiving RPC messages.
 */
public class RemoteGateway extends WisperObject
{
    private Channel channel;
    private Gateway gateway;

    public static RPCClass registerRpcClass()
    {
        RPCClass rpcClass = new RPCClass(RemoteGateway.class, "wisper.Gateway");
        rpcClass.addInstanceMethod(new RPCClassMethod("setChannel", "setChannel", RPCMethodParameterType.INSTANCE));
        return rpcClass;
    }

    /**
     * The channel is set remotely by the caller, i.e. from the JS side or by the ad.
     * This will enable the JS side to talk to the Gateway assigned to this WisperWebView.
     *
     * @param channel the provided channel to be assigned, i.e. WebViewChannel
     */
    public void setChannel(Channel channel)
    {
        this.channel = channel;
        channel.setGateway(gateway);
        gateway.setCallback(channel);
    }

    @Override
    public void setRemoteObjectController(RemoteObjectController remoteObjectController)
    {
        super.setRemoteObjectController(remoteObjectController);
        gateway = new Gateway(new GatewayCallback()
        {
            @Override
            public void gatewayReceivedMessage(AbstractMessage message)
            {
                if (channel != null)
                {
                   //Not implemented by channel
                }
            }

            @Override
            public void gatewayGeneratedMessage(String message)
            {
                if (channel!=null)
                {
                    channel.sendMessage(message);
                }

            }
        });
    }
}
