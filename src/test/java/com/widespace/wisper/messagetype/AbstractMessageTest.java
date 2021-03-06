package com.widespace.wisper.messagetype;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.widespace.wisper.messagetype.AbstractMessage.deserialize;
import static com.widespace.wisper.messagetype.AbstractMessage.serialize;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the JSON utilities provided by AbstractMessage.
 * <p/>
 * Created by oskar on 2016-05-17.
 */
public class AbstractMessageTest
{
    @Test
    public void testSerializeNull()
    {
        // TODO: shouldn't this be JSONObject.NULL?
        assertThat(serialize(null), is((Object) null));
    }

    @Test
    public void testSerializePrimitives()
    {
        assertThat(serialize("foo"), is(equalTo((Object) "foo")));
        assertThat(serialize("foo"), is(equalTo((Object) "foo")));

        BigDecimal big = new BigDecimal(17);
        assertThat(serialize(big), is(equalTo((Object) big)));
    }

    @Test
    public void testSerializeLists()
    {
        JSONArray list = (JSONArray) serialize(asList(1, 2));
        assertThat(list.length(), is(2));
    }

    @Test
    public void testSerializeArrays()
    {
        JSONArray array = (JSONArray) serialize(new Integer[]{1, 2, 3});
        assertThat(array.length(), is(3));
    }

    @Test
    public void testSerializeMaps()
    {

        JSONObject fooAndBar = (JSONObject) serialize(new HashMap<String, Integer>()
        {
            {
                put("foo", 1);
                put("bar", 2);
            }
        });

        assertThat(fooAndBar, is(instanceOf(JSONObject.class)));

        assertThat(fooAndBar.length(), is(2));
        assertThat(fooAndBar.getInt("foo"), is(1));
        assertThat(fooAndBar.getInt("bar"), is(2));
    }

    @Test
    public void testSerializeJSON()
    {
        JSONArray array = new JSONArray();
        assertThat(serialize(array), is((Object) array));

        JSONObject object = new JSONObject();
        assertThat(serialize(object), is((Object) object));
    }

    @Test
    public void testSerializeUnserializable()
    {
        assertThat(serialize(new Object()), is((Object) null));
    }

    @Test
    public void testDeserializeNull()
    {
        assertThat(deserialize(JSONObject.NULL), is((Object) null));
    }

    @Test
    public void testDeserializePrimitives()
    {
        assertThat(deserialize("foo"), is(equalTo((Object) "foo")));
        assertThat(deserialize(1337), is(equalTo((Object) 1337)));
        assertThat(deserialize(47.11), is(equalTo((Object) 47.11)));
    }

    @Test
    public void testDeserializeArrays()
    {
        Object[] array = new Object[]{1, 2};
        assertThat(deserialize(new JSONArray(array)), is(equalTo((Object) array)));

        assertThat(deserialize(new JSONArray(asList(1, 2))), is(equalTo((Object) array)));

        assertThat((Object[]) deserialize(new JSONArray(Collections.singletonList(new JSONArray()))), is(equalTo(new Object[]{new Object[0]})));
    }

    @Test
    public void testDeserializeMaps()
    {
        final Object[] array = {41, "nick"};

        final Map<String, Object> internalMap = new HashMap<String, Object>()
        {{
            put("name", "Oskar");
        }};

        Map<String, Object> map = new HashMap<String, Object>()
        {
            {
                put("foo", "charlie");
                put("baz", internalMap);
            }
        };

        JSONObject object = new JSONObject()
        {
            {
                put("foo", "charlie");
                put("bar", new JSONArray(array));
                put("baz", new JSONObject(internalMap));
            }
        };

        Map<String, Object> deserialized = (Map<String, Object>) deserialize(object);

        assertThat(deserialized, is(instanceOf(Map.class)));

        // TODO: do we really want to deal with these annoying arrays?
        // Check the array element separately, since array.equals(other) is the same as array == other.
        // Then remove the array for the following comparison.
        assertThat((Object[]) deserialized.get("bar"), is(equalTo(array)));
        deserialized.remove("bar");

        assertThat(deserialized, is(equalTo((Object) map)));
    }

    @Test
    public void testDeserializeUndeserializable()
    {
        Object object = new Object();
        assertThat(deserialize(object), is(object));
    }
}
