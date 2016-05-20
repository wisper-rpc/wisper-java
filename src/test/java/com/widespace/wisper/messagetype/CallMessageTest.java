package com.widespace.wisper.messagetype;

import org.json.JSONObject;
import org.junit.Test;

import static com.widespace.wisper.messagetype.AbstractMessage.EMPTY_PARAMS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by oskar on 2016-05-20.
 */
public class CallMessageTest
{

    @Test
    public void testNotificationEquals() throws Exception
    {
        Notification n1 = new Notification("foo", new Object[]{1337, "bar"});
        Notification n2 = new Notification("foo", new Object[]{1337, "bar"});
        Notification n3 = new Notification("foo", new Object[]{1337});

        assertThat(n1, is(not(equalTo((Object) new Request("foo")))));

        assertThat(n1, is(equalTo(n2)));

        assertThat(n1, is(not(equalTo(n3))));
    }

    @Test
    public void testRequestEquals() throws Exception
    {
        Request n1 = new Request("foo", new Object[]{1337, "bar"});
        Request n2 = new Request("foo", new Object[]{1337, "bar"});
        Request n3 = new Request("foo", new Object[]{1337});

        assertThat(n1, is(not(equalTo((Object) new Notification("foo")))));

        assertThat(n1, is(equalTo(n2)));

        assertThat(n1, is(not(equalTo(n3))));
    }

    @Test
    public void testHashCode()
    {
        Notification fooNot = new Notification("foo");
        Notification barNot = new Notification("bar");
        Request fooReq = new Request("foo");

        assertThat(fooNot.hashCode(), is(new Notification("foo").hashCode()));
        assertThat(fooNot.hashCode(), is(fooReq.hashCode()));

        assertThat(fooNot.hashCode(), is(not(barNot.hashCode())));

        assertThat(fooNot.hashCode(), is(not(barNot.hashCode())));
    }

    @Test
    public void testRequestToJson()
    {
        assertThat(new Notification("name").toJsonString(), is(equalTo(new JSONObject()
        {
            {
                put("method", "name");
                put("params", EMPTY_PARAMS);
            }
        }.toString())));

        // TODO: invalid request missing the id field
        assertThat(new Request("name").toJsonString(), is(equalTo(new JSONObject()
        {
            {
                put("method", "name");
                put("params", EMPTY_PARAMS);
            }
        }.toString())));
    }
}
