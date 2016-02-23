package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class WisperInstanceCreatorTest
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

        new WisperInstanceCreator(mock(WisperClassModel.class), request);
    }

    @Test
    public void createsInstanceOnCorrectRequest() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
        WisperInstanceCreator creator = new WisperInstanceCreator(RoutesTestObject.registerRpcClass(), testObjectCreateRequest());
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                assertThat(instanceModel, is(notNullValue()));
            }
        });
    }

    @Test
    public void callsRequestResponseBlockOnCreate() throws Exception
    {
        Request request = testObjectCreateRequest();
        final boolean[] responseBlockCalled = new boolean[]{false};
        request.setResponseBlock(new ResponseBlock()
        {

            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                responseBlockCalled[0] = true;
            }
        });

        WisperInstanceCreator creator = new WisperInstanceCreator(RoutesTestObject.registerRpcClass(), request);
        creator.create(mock(RemoteInstanceCreatorCallback.class));
        assertThat(responseBlockCalled[0], is(true));
    }

    //TODO: TO BE PASSED WHEN METHOD CALLER IS IMPLEMENTED
    @Ignore
    @Test
    public void testGivenCustomConstructors_creatorCanHandleIt() throws Exception
    {
        Request request = new Request(new JSONObject("{ \"method\" : \"whatever.whatever.thing~\", \"params\" : [\"testString\"], \"id\": \"ABCD\" }"), null);
        WisperInstanceCreator creator = new WisperInstanceCreator(RoutesTestObject.registerRpcClass(), request);
        final Object[] result = new Object[2];
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                result[0] = instanceModel;
                result[1] = ex;
            }
        });

        assertThat(result[0], is(notNullValue()));
    }

    //--------------------------
    private Request testObjectCreateRequest()
    {
        Request creationRequest = new Request();
        creationRequest.setIdentifier("ABCD1");
        creationRequest.setMethod("whatever.whatever.thing~");
        return creationRequest;
    }
}