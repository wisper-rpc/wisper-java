package com.widespace.wisper.base;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.route.ClassRouter;

/**
 * This class could be extended by all the classes that require registration to the RPC
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public abstract class WisperObject implements Wisper
{
    protected ClassRouter classRouter;

    /**
     * This method must be implemented by any object desiring to register itself as RPC
     * unfortunately prior to Java 8 we cannot have static methods in Interfaces which is why we need to take this approach instead.
     *
     * @return WisperClassModel an instance of the RPC class model object containing the models of methods
     */
    public static WisperClassModel registerRpcClass()
    {
        return null;
    }


    @Override
    public void destruct()
    {

    }

    @Override
    public void setClassRouter(ClassRouter classRouter)
    {
        this.classRouter = classRouter;
    }
}
