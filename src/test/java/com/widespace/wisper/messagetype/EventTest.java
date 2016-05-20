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
    final Object[] NO_PARAMS = new Object[0];

    @Test(expected = IllegalArgumentException.class)
    public void createStaticWithMissingExclamation()
    {
        // method name missing '!'
        new Event("foo", "foo", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStaticWithInstanceSignature()
    {
        // the method name should be "foo!"
        new Event("foo:!", "foo", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createInstanceWithMissingColon()
    {
        // the method name should be "foo:!"
        new Event("foo!", "id", "foo", 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createInstanceWithWrongNumberOfParams()
    {
        new Event("foo:!", NO_PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStaticWithWrongNumberOfParams()
    {
        new Event("foo!", NO_PARAMS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithBadEventName()
    {
        new Event("foo", NO_PARAMS);
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
}
