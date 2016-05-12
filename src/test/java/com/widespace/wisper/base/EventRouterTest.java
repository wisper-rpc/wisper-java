package com.widespace.wisper.base;

import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.route.EventRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.RemoteObjectEventInterface;
import com.widespace.wisper.route.Router;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;

/**
 * Created by ehssanhoorvash on 09/05/16.
 */
public class EventRouterTest
{


    @Test
    public void testAddInstance_addsInstanceToRemoteObjects() throws Exception
    {
        EventRouter eventRouter = new EventRouter(WisperRemoteObject.class);

        WisperRemoteObject addedRemoteObject = mock(WisperRemoteObject.class);
        eventRouter.addInstance("id123", addedRemoteObject);

        assertThat(eventRouter.getRemoteObjects(), is(notNullValue()));
        assertThat(eventRouter.getRemoteObjects().isEmpty(), is(false));
        assertThat(eventRouter.getRemoteObjects().size(), is(1));
        assertThat(eventRouter.getRemoteObjects().containsKey("id123"), is(true));
    }

    @Test
    public void testRemoveInstance_removesWisperRemoteObject() throws Exception
    {
        EventRouter eventRouter = new EventRouter(WisperRemoteObject.class);

        WisperRemoteObject wisperRemoteObject = mock(WisperRemoteObject.class);
        eventRouter.addInstance("1234",wisperRemoteObject);

        assertThat(eventRouter.getRemoteObjects().size(),is(1));
        eventRouter.removeInstance("1234");
        assertThat(eventRouter.getRemoteObjects().size(),is(0));
    }

}