package com.widespace.wisper.messagetype;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for the Event message type.
 */
public class EventTest
{
    @Test(expected = IllegalArgumentException.class)
    public void createWithOneParameter()
    {
        new Event("foo!", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithFourParameters()
    {
        new Event("foo!", 1, 2, 3, 4);
    }

    @Test
    public void createFromNotification()
    {
        Event e1 = new Event(new Notification("foo!", new Object[]{ "bar", "baz" }));

        // Not an instance event
        assertThat(e1.getInstanceIdentifier(), is((String) null));

        assertThat(e1.getName(), is(equalTo("bar")));
        assertThat((String) e1.getValue(), is(equalTo("baz")));

        Event e2 = new Event(new Notification("foo:!", new Object[]{ "id", "bar", "baz" }));

        // Instance event
        assertThat(e2.getInstanceIdentifier(), is(equalTo("id")));

        assertThat(e2.getName(), is(equalTo("bar")));
        assertThat((String) e2.getValue(), is(equalTo("baz")));
    }

    @Test
    public void createWithTwoParameters()
    {
        Object[] params = {"eventName", 1337};

        Event e = new Event("foo!", params);

        assertThat(e.getMethodName(), is(equalTo("foo!")));
        assertThat(e.getParams(), is(equalTo(params)));

        // Not an instance event
        assertThat(e.getInstanceIdentifier(), is((String) null));

        assertThat(e.getName(), is(equalTo("eventName")));
        assertThat((Integer) e.getValue(), is(1337));
    }

    @Test(expected = ClassCastException.class)
    public void createWithTwoParameterBadTypes()
    {
        new Event("foo!", 13, 37);
    }

    @Test
    public void createWithThreeParameters()
    {
        Object[] params = {"instanceId", "eventName", 1337};

        Event e = new Event("foo:!", params);

        assertThat(e.getMethodName(), is(equalTo("foo:!")));
        assertThat(e.getParams(), is(equalTo(params)));

        // Instance event
        assertThat(e.getInstanceIdentifier(), is(equalTo("instanceId")));

        assertThat(e.getName(), is(equalTo("eventName")));
        assertThat((Integer) e.getValue(), is(1337));
    }

    @Test(expected = ClassCastException.class)
    public void createWithThreeParameterBadTypes1()
    {
        new Event("foo:!", "foo", 13, 37);
    }

    @Test(expected = ClassCastException.class)
    public void createWithThreeParameterBadTypes2()
    {
        new Event("foo:!", 13, "foo", 37);
    }

}
