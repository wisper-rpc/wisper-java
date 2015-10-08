package com.widespace.wisper.messagetype;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Abstract message type calss for RPC. All message types will be children of this abstract class.
 * <p/>
 * This abstract class will contain the json representation of the message which is passed in, for instance, by a request to rpc controller.
 * Created by Ehssan Hoorvash on 23/05/14.
 */
public abstract class AbstractMessage
{
    protected JSONObject jsonForm;

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
    public abstract String toJsonString();

    /**
     * marshalling of JSON representation of this message
     *
     * @return a json object representation of this message
     */
    public JSONObject toJson()
    {
        return jsonForm;
    }

    /**
     * Utility method that converts a json array to an Object array
     *
     * @param jsonArray json array
     * @return an object array filled with the same objects as existed in json array
     * @throws JSONException If json representation of the array could not be parsed
     */
    protected Object[] jsonArrayToArray(JSONArray jsonArray) throws JSONException
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


    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException
    {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL)
        {
            retMap = toMap(json);
        }
        return retMap;
    }

    private Map<String, Object> toMap(JSONObject jsonObject) throws JSONException
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

    private List<Object> toList(JSONArray array) throws JSONException
    {
        return Arrays.asList(jsonArrayToArray(array));
    }
}
