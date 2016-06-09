package com.widespace.wisper.base;

import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.route.GatewayRouter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by ehssanhoorvash on 09/05/16.
 */
class MyWisperRemoteObject extends WisperRemoteObject
{
    public static Event recievedStaticEvent;
    public Event recievedEvent;

    /**
     * Constructs the object with the given map name and gatewayRouter.
     *
     * @param mapName       the map name to be used for this remote object.
     * @param gatewayRouter The gatewayRouter through which the remote object is accessible.
     */
    public MyWisperRemoteObject(@NotNull String mapName, @NotNull GatewayRouter gatewayRouter)
    {
        super(mapName, gatewayRouter);
    }


    public static void handleStaticEvent(Event event)
    {
        recievedStaticEvent = event;
    }

    @Override
    public void handleInstanceEvent(Event event)
    {
        recievedEvent = event;
    }

}
