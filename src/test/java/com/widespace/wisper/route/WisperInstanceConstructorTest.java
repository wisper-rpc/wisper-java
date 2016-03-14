package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class WisperInstanceConstructorTest
{
    @Before
    public void setUp() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
        RoutesTestObject.reset();
    }

    @Test(expected = WisperException.class)
    public void wrongMessageCallTypeIsNotAccepted() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:call");
        request.setIdentifier("ABCD1");

        new WisperInstanceConstructor(mock(ClassRouter.class), mock(WisperClassModel.class), request);
    }

    @Test
    public void createsInstanceOnCorrectRequest() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), testObjectCreateRequest());
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

        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), request);
        creator.create(mock(RemoteInstanceCreatorCallback.class));

        assertThat(responseBlockCalled[0], is(true));
    }

    @Test
    public void instanceCreation_setsClassRouterOnTheInstance() throws Exception
    {
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), testObjectCreateRequest());
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                RoutesTestObject instance = (RoutesTestObject) instanceModel.getInstance();
                assertThat(instance.getClassRouter(), is(notNullValue()));
            }
        });

    }

    @Test
    public void testGivenCustomConstructors_creatorCanHandleIt() throws Exception
    {
        String CONSTRUCTOR_PARAM_VALUE = "testString";
        Request request = new Request(new JSONObject("{ \"method\" : \"whatever.whatever.thing~\", \"params\" : [\"" + CONSTRUCTOR_PARAM_VALUE + "\"], \"id\": \"ABCD\" }"), null);
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), request);
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

        WisperInstanceModel instance = (WisperInstanceModel) result[0];
        assertThat(instance, is(notNullValue()));
        assertThat(instance.getInstance(), is(instanceOf(RoutesTestObject.class)));
        assertThat(((RoutesTestObject) instance.getInstance()).getTestId(), is(CONSTRUCTOR_PARAM_VALUE));
    }

    @Test
    public void givenConstructorWithWrongParamType_exceptionIsThrown() throws Exception
    {
        Double CONSTRUCTOR_WRONG_TYPE = 112.03;
        Request request = new Request(new JSONObject("{ \"method\" : \"whatever.whatever.thing~\", \"params\" : [" + CONSTRUCTOR_WRONG_TYPE + "], \"id\": \"ABCD\" }"), null);
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), request);
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

        WisperInstanceModel instance = (WisperInstanceModel) result[0];
        WisperException exception = (WisperException) result[1];
        assertThat(instance, is(nullValue()));
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getErrorCode(), is(Error.CONSTRUCTOR_NOT_FOUND.getCode()));
    }

    @Test
    public void givenConstructorWithWrongParamNumbers_exceptionIsThrown() throws Exception
    {
        String CONSTRUCTOR_WRONG_NUMBER = "param1, param2";
        Request request = new Request(new JSONObject("{ \"method\" : \"whatever.whatever.thing~\", \"params\" : [" + CONSTRUCTOR_WRONG_NUMBER + "], \"id\": \"ABCD\" }"), null);
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), request);
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

        WisperInstanceModel instance = (WisperInstanceModel) result[0];
        WisperException exception = (WisperException) result[1];
        assertThat(instance, is(nullValue()));
        assertThat(exception, is(notNullValue()));
        assertThat(exception.getErrorCode(), is(Error.CONSTRUCTOR_NOT_FOUND.getCode()));
    }

    @Test
    public void testGivenConstructor_returnsInitializedPropertiesInResponse() throws Exception
    {
        Request request = testObjectCreateRequest();
        request.setParams(new Object[]{"something"});
        final Object[] responseBlockResponse = new Object[1];
        request.setResponseBlock(new ResponseBlock()
        {

            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                responseBlockResponse[0] = response;
            }
        });

        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class),RoutesTestObject.registerRpcClass(), request);
        creator.create(mock(RemoteInstanceCreatorCallback.class));

        // Check we get a response
        Response response = (Response) responseBlockResponse[0];
        assertThat(response, is(notNullValue()));
        assertThat(response.getIdentifier(), is(equalTo(request.getIdentifier())));

        //Check we get id,props in response
        HashMap<String, Object> responseResult = (HashMap<String, Object>) response.getResult();
        assertThat(responseResult, is(notNullValue()));
        assertThat(responseResult.containsKey("id"), is(true));
        assertThat(responseResult.containsKey("props"), is(true));

        // Check props includes "prop" (the instance property in test object) and the value is set correctly.
        HashMap<String, Object> props = (HashMap<String, Object>) responseResult.get("props");
        assertThat(props.containsKey("prop"), is(true));
        assertThat((String) props.get("prop"), is(equalTo("set-by-constructor")));
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