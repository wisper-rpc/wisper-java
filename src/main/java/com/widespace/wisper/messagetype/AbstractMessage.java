package com.widespace.wisper.messagetype;

import com.widespace.wisper.messagetype.error.RPCError;
import com.widespace.wisper.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static com.widespace.wisper.base.Constants.*;

/**
 * Abstract message type calss for RPC. All message types will be children of this abstract class.
 * <p/>
 * This abstract class will contain the json representation of the message which is passed in, for instance, by a request to rpc controller.
 * Created by Ehssan Hoorvash on 23/05/14.
 */
public abstract class AbstractMessage
{
    public static final Object[] EMPTY_PARAMS = new Object[0];

    /**
     * Returns the message type
     *
     * @return a member of RPCMessageType which represents the type of this message
     */
    public abstract RPCMessageType type();

    /**
     * JSON to string method for this message.
     *
     * @return a String representation of this message
     */
    public String toJsonString() throws JSONException
    {
        return toJson().toString();
    }

    /**
     * marshalling of JSON representation of this message
     *
     * @return a json object representation of this message
     */
    public abstract JSONObject toJson() throws JSONException;


    /**
     * Utility method that converts a json array to an Object array
     *
     * @param jsonArray json array
     * @return an object array filled with the same objects as existed in json array
     * @throws JSONException If json representation of the array could not be parsed
     */
    @NotNull
    static Object[] jsonArrayToArray(@NotNull JSONArray jsonArray) throws JSONException
    {

        return toList(jsonArray).toArray();
    }


    @NotNull
    private static Map<String, Object> toMap(@NotNull JSONObject object) throws JSONException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        for (String key : iterable((Iterator<String>) object.keys()))
        {
            map.put(key, deserialize(object.get(key)));
        }

        return map;
    }

    /**
     * Get an Iterable from an Iterator.
     *
     * @param iterator an iterator
     * @return an iterable
     */
    @NotNull
    private static <T> Iterable<? extends T> iterable(@NotNull final Iterator<T> iterator)
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return iterator;
            }
        };
    }

    @NotNull
    private static List<Object> toList(@NotNull JSONArray jsonArray) throws JSONException
    {

        ArrayList<Object> arrayList = new ArrayList<Object>();

        int len = jsonArray.length();
        for (int i = 0; i < len; i++)
        {
            arrayList.add(deserialize(jsonArray.get(i)));
        }

        return arrayList;
    }

    public String getIdentifier()
    {
        return null;
    }

    private JSONArray serializeList(@NotNull List list)
    {
        JSONArray array = new JSONArray();
        for (Object object : list)
        {
            array.put(serialize(object));
        }
        return array;
    }

    @Nullable
    protected Object serialize(@Nullable Object newResult)
    {
        if (newResult == null)
        {
            return JSONObject.NULL;
        }

        if (newResult.getClass().isArray())
        {
            return serializeList(Arrays.asList((Object[]) newResult));
        }
        else if (newResult instanceof List)
        {
            return serializeList((List) newResult);
        }
        else if (ClassUtils.isPrimitive(newResult.getClass()) || newResult.getClass().equals(String.class))
        {
            return newResult;
        }
        else if (newResult.getClass().equals(JSONObject.class) || newResult.getClass().equals(JSONArray.class))
        {
            return newResult;
        }
        else if (newResult.getClass().isAssignableFrom(Map.class) || newResult.getClass().isAssignableFrom(HashMap.class))
        {
            return new JSONObject((Map) newResult);
        }
        else if (Number.class.isAssignableFrom(newResult.getClass()))
        {
            return newResult;
        }
        else if (newResult instanceof RPCError)
        {
            JSONObject json = new JSONObject();
            RPCError error = (RPCError) newResult;
            json.put(CODE, error.getCode());
            json.put(DOMAIN, error.getDomain());
            json.put(NAME, error.getName() == null ? "" : error.getName());
            json.put(DATA, error.getData() == null ? "" : serialize(error.getData()));
            json.put(MESSAGE, error.getMessage() == null ? "" : error.getMessage());
            json.put(UNDERLYING_ERROR, error.getUnderlyingError() == null ? "" : serialize(error.getUnderlyingError()));

            return json;
        }
        else
        {
            return null;
        }
    }

    /**
     * Deserialize the given JSONArray to an Object array.
     *
     * @param array the JSON array
     * @return a plain array
     */
    @NotNull
    static Object[] deserializeArray(@NotNull JSONArray array)
    {
        return toList(array).toArray();
    }

    @Nullable
    static Object deserialize(@NotNull Object result)
    {
        if (result == JSONObject.NULL)
        {
            return null;
        }

        if (result instanceof JSONArray)
        {
            return toList((JSONArray) result);
        }

        if (result instanceof JSONObject)
        {
            return toMap((JSONObject) result);
            //TODO: Handle RPCError ??
        }

        // Primitives
        return result;
    }
}
