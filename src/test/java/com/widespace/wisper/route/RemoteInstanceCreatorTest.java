package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class RemoteInstanceCreatorTest
{
    @Test(expected = WisperException.class)
    public void wrongMessageTypeIsNotAccepted() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:call");
        request.setIdentifier("ABCD1");

        new RemoteInstanceCreator(mock(WisperClassModel.class), request);
    }

    @Test
    public void createsInstanceOnCorrectRequest() throws Exception
    {

    }
}