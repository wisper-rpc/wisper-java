package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.WisperException;

/**
 * Function routers are meant to help when a simple task is to be taken care of without the need for registering a class for that.
 * routeMessage() of the Router has to be overridden (mandatory), and the exceptions must be handled inside the routeMessage so they are
 * always converted into a WisperException.
 */
public abstract class FunctionRouter extends Router
{
    @Override
    public abstract void routeMessage(AbstractMessage message, String path) throws WisperException;
}
