package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.WisperException;

public abstract class FunctionRouter extends Router
{
    @Override
    public abstract void routeMessage(AbstractMessage message, String path) throws WisperException;

}
