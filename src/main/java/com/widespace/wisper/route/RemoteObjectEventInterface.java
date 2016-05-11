package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.Event;

/**
 * Created by patrik on 11/05/16.
 */
public interface RemoteObjectEventInterface
{
    void handleStaticEvent(Event event);
    void handleInstanceEvent(Event event);
}
