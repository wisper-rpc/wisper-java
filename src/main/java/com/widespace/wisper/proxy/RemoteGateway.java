package com.widespace.wisper.proxy;

import com.widespace.wisper.base.WisperObject;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.route.Channel;
import com.widespace.wisper.route.ClassRouter;


/**
 * This class provides the link between the webView and the remote object controller as of sending and receiving RPC messages.
 */
public class RemoteGateway extends WisperObject
{
    private Channel channel;
    private Gateway gateway;


    public static WisperClassModel registerRpcClass()
    {
        WisperClassModel wisperClassModel = new WisperClassModel(RemoteGateway.class, "wisper.Gateway");
        wisperClassModel.addInstanceMethod(new WisperMethod("setChannel", "setChannel", WisperParameterType.INSTANCE));
        return wisperClassModel;
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
    public void setClassRouter(ClassRouter classRouter)
    {

    }
}
