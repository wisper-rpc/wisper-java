package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


/**
 * Created by patrik on 2016-10-10.
 */

public class WisperParamTypeAsyncReturnTest {

    private static boolean gotResponse = false;

    @Before
    public void setUp() throws Exception
    {
        RoutesTestObject.reset();
        gotResponse = false;
    }

    @Test
    public void givenMethodWithAsyncReturn_AsyncReturnIsInjected() throws Exception
    {
        String methodName = "methodWithAsyncReturn";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));
        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "success"});
        request.setResponseBlock(new ResponseBlock() {
            @Override
            public void perform(Response response, RPCErrorMessage error) {
                gotResponse = true;
                assertThat("Response carries 'success' as its result", response.getResult() == "success");
            }
        });

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));

        ClassRouter mock = mock(ClassRouter.class);
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock, instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat("Request got a response", gotResponse == true);
    }

    @Test
    public void givenMethodWithAsyncReturnParamFirst_AsyncReturnIsInjected() throws Exception
    {
        String methodName = "methodWithAsyncReturnFirst";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));
        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "success"});
        request.setResponseBlock(new ResponseBlock() {
            @Override
            public void perform(Response response, RPCErrorMessage error) {
                gotResponse = true;
                assertThat("Response carries 'success' as its result", response.getResult() == "success");
            }
        });

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));

        ClassRouter mock = mock(ClassRouter.class);
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock, instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat("Request got a response", gotResponse == true);
    }

    @Test
    public void givenMethodWithContextAndAsyncReturn_AsyncReturnAndContextAreInjected() throws Exception
    {
        String methodName = "methodWithContextAndAsyncReturn";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));
        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "success"});
        request.setResponseBlock(new ResponseBlock() {
            @Override
            public void perform(Response response, RPCErrorMessage error) {
                gotResponse = true;
                assertThat("Response carries 'success' as its result", response.getResult() == "success");
            }
        });

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));

        ClassRouter mock = mock(ClassRouter.class);
        Object fakeAndroidContext = "android_context";
        when(mock.getGatewayExtra("context")).thenReturn(fakeAndroidContext);
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock, instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat(actualInstance.methodCalledWithConetxt(fakeAndroidContext), is(true));
        assertThat("Request got a response", gotResponse == true);
    }


    @Test
    public void givenMethodWithContextAndAsyncReturnParamFirst_AsyncReturnAndContextAreInjected() throws Exception
    {
        String methodName = "methodWithContextAndAsyncReturnFirst";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));
        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "success"});
        request.setResponseBlock(new ResponseBlock() {
            @Override
            public void perform(Response response, RPCErrorMessage error) {
                gotResponse = true;
                assertThat("Response carries 'success' as its result", response.getResult() == "success");
            }
        });

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));

        ClassRouter mock = mock(ClassRouter.class);
        Object fakeAndroidContext = "android_context";
        when(mock.getGatewayExtra("context")).thenReturn(fakeAndroidContext);
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock, instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat(actualInstance.methodCalledWithConetxt(fakeAndroidContext), is(true));
        assertThat("Request got a response", gotResponse == true);
    }

    //--------------------------
    private WisperInstanceModel createWisperInstanceForTestObject(String mapName) throws InterruptedException
    {
        Request creationRequest = new Request(mapName + "~");
        creationRequest.setIdentifier("ABCD1");

        final WisperInstanceModel[] _instanceModel = new WisperInstanceModel[1];
        WisperInstanceConstructor creator = new WisperInstanceConstructor(mock(ClassRouter.class), RoutesTestObject.registerRpcClass(), creationRequest);
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                _instanceModel[0] = instanceModel;
            }
        });

        if (_instanceModel[0] == null)
        {
            Thread.sleep(400);
        }

        return _instanceModel[0];
    }

}
