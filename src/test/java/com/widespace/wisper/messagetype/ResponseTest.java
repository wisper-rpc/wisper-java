package com.widespace.wisper.messagetype;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ehssanhoorvash on 03/02/16.
 */
public class ResponseTest
{
    private String SAMPLE_REQUEST_ID = "abcs1234";

    @Test
    public void testResponseCanBeMadeWithJson() throws Exception
    {
        Request creationRequest = new Request(new JSONObject("{ \"method\" : \"wisp.test.ControllerTest~\", \"params\" : [\"testString\"], \"id\": \"" + SAMPLE_REQUEST_ID + "\" }"), null);


    }
}