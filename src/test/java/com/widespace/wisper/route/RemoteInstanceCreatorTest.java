package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class RemoteInstanceCreatorTest
{
    @Before
    public void setUp() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
    }

    @Test(expected = WisperException.class)
    public void wrongMessageCallTypeIsNotAccepted() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:call");
        request.setIdentifier("ABCD1");

        new RemoteInstanceCreator(mock(WisperClassModel.class), request);
    }

    @Test
    public void createsInstanceOnCorrectRequest() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
        RemoteInstanceCreator creator = new RemoteInstanceCreator(RoutesTestObject.registerRpcClass(), testObjectCreateRequest());
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                assertThat(instanceModel, is(notNullValue()));
            }
        });
    }

    private Request testObjectCreateRequest()
    {
        Request creationRequest = new Request();
        creationRequest.setIdentifier("ABCD1");
        creationRequest.setMethod("whatever.whatever.thing~");
        return creationRequest;
    }
}