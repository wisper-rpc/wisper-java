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
    static Object[] jsonArrayToArray(@Nullable JSONArray jsonArray) throws JSONException
    {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        if (jsonArray != null)
        {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++)
            {
                //in case of string and primitive (number)
                Object theParam = jsonArray.get(i);

                //in case of ARRAY
                if (theParam instanceof JSONArray)
                {
                    theParam = toList((JSONArray) theParam);
                }

                //in case of Hashmap
                if (theParam instanceof JSONObject)
                {
                    theParam = jsonToMap((JSONObject) theParam);
                }

                arrayList.add(theParam);
            }
        }

        return arrayList.toArray();
    }


    private static Map<String, Object> jsonToMap(JSONObject json) throws JSONException
    {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL)
        {
            retMap = toMap(json);
        }
        return retMap;
    }

    private static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException
    {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<?> keysItr = jsonObject.keys();
        while (keysItr.hasNext())
        {
            String key = (String) keysItr.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONArray)
            {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject)
            {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException
    {
        return Arrays.asList(jsonArrayToArray(array));
    }

    public String getIdentifier()
    {
        return null;
    }

    protected static Object serialize(Object newResult)
    {
        if (newResult == null)
        {
            return null;
        }

        if (newResult.getClass().isArray())
        {
            JSONArray array = new JSONArray();

            for (Object object : (Object[]) newResult)
            {
                array.put(serialize(object));
            }

            return array;
        }
        else if (newResult instanceof List)
        {
            JSONArray array = new JSONArray();
            for (java.lang.Object object : (List) newResult)
            {
                array.put(serialize(object));
            }
            return array;
        }
        else if (ClassUtils.isPrimitive(newResult.getClass()) || newResult.getClass().equals(String.class))
        {
            return newResult;
        }
        else if (newResult.getClass().equals(JSONObject.class) || newResult.getClass().equals(JSONArray.class))
        {
            return newResult;
        }
        else if (Map.class.isAssignableFrom(newResult.getClass()))
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

    static Object deserialize(Object result)
    {
        if (result == JSONObject.NULL)
        {
            return null;
        }

        if (result instanceof JSONArray)
        {
            ArrayList<Object> arrayList = new ArrayList<Object>();
            JSONArray jsonArray = (JSONArray) result;
            for (int i = 0; i < jsonArray.length(); i++)
            {
                arrayList.add(deserialize(jsonArray.get(i)));
            }

            return arrayList.toArray(new Object[arrayList.size()]);
        }

        if (result instanceof JSONObject)
        {
            JSONObject json = (JSONObject) result;
            HashMap<String, Object> map = new HashMap<String, Object>();
            Iterator keys = json.keys();
            while (keys.hasNext())
            {
                String key = (String) keys.next();
                map.put(key, deserialize(json.get(key)));
            }

            return map;

            //TODO: Handle RPCError ??
        }

        return result;
    }
}
