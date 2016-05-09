package com.widespace.wisper.base;

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

    @Before
    public void setUp() throws Exception
    {


    }

    @Test
    public void testAddInstance_addsInstanceToRemoteObjects() throws Exception
    {
        EventRouter eventRouter = new EventRouter(mock(WisperRemoteObject.class));

        WisperRemoteObject addedRemoteObject = mock(WisperRemoteObject.class);
        eventRouter.addInstance("id123", addedRemoteObject);

        assertThat(eventRouter.getRemoteObjects(), is(notNullValue()));
        assertThat(eventRouter.getRemoteObjects().isEmpty(), is(false));
        assertThat(eventRouter.getRemoteObjects().size(), is(1));
        assertThat(eventRouter.getRemoteObjects().containsKey("id123"), is(true));
    }
}