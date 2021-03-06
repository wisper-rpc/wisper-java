package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class WisperMethodCallerTest
{

    @Before
    public void setUp() throws Exception
    {
        RoutesTestObject.reset();
    }

    @Test(expected = WisperException.class)
    public void givenWrongMessageType_ThrowsException() throws Exception
    {
        Request request = new Request("a.b.c:~");
        new WisperMethodCaller(mock(ClassRouter.class), mock(WisperClassModel.class), request);
    }


    @Ignore("Needs re-writing with a real WisperObj")
    @Test
    public void callDispatchesStaticMethods() throws Exception
    {
        String methodName = "methodName";
        Request request = new Request("whatever.whatever.thing." + methodName);

        WisperMethodCaller methodCaller = spy(new WisperMethodCaller(mock(ClassRouter.class), mock(WisperClassModel.class), request));
        methodCaller.call();
        verify(methodCaller).callStatic(any(WisperMethod.class));
    }

    @Ignore("Needs re-writing with real WirperObj")
    @Test
    public void callDispatchesInstanceMethods() throws Exception
    {
        String methodName = "methodName";
        Request request = new Request("whatever.whatever.thing:" + methodName);

        WisperMethodCaller methodCaller = spy(new WisperMethodCaller(mock(ClassRouter.class), mock(WisperClassModel.class), request));
        methodCaller.call();
        verify(methodCaller).callInstance(any(WisperMethod.class));
    }

    @Test
    public void givenStaticMethodRequest_CallsStaticMethodOnTheActualClass() throws Exception
    {
        String methodName = "append";
        Request request = new Request("whatever.whatever.thing." + methodName, new Object[]{"str1", "str2"});
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock(ClassRouter.class), RoutesTestObject.registerRpcClass(), request);
        assertThat(RoutesTestObject.staticMethodCalled(), is(false));
        methodCaller.call();

        assertThat(RoutesTestObject.staticMethodCalled(), is(true));
    }

    @Test
    public void givenInstanceMethodRequest_CallsInstanceMethodOnTheActualInstance() throws Exception
    {
        String methodName = "append";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));

        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "str1", "str2"});

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock(ClassRouter.class), instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat(actualInstance.instanceMethodCalled(), is(true));
    }

    @Test
    public void givenMethodWithContextParamType_contextIsInjectedAtCallTime() throws Exception
    {
        String methodName = "methodWithContext";

        WisperInstanceModel instanceModel = createWisperInstanceForTestObject("whatever.whatever.thing");
        WisperInstanceRegistry.sharedInstance().addInstance(instanceModel, mock(Router.class));
        Request request = new Request("whatever.whatever.thing:" + methodName, new Object[]{instanceModel.getInstanceIdentifier(), "str1"});

        RoutesTestObject actualInstance = (RoutesTestObject) instanceModel.getInstance();
        assertThat(actualInstance.instanceMethodCalled(), is(false));

        Object fakeAndroidContext = "dhjshdjdsh";
        ClassRouter mock = mock(ClassRouter.class);
        when(mock.getGatewayExtra("context")).thenReturn(fakeAndroidContext);
        WisperMethodCaller methodCaller = new WisperMethodCaller(mock, instanceModel.getWisperClassModel(), request);
        methodCaller.call();

        assertThat(actualInstance.methodCalledWithConetxt(fakeAndroidContext), is(true));
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
