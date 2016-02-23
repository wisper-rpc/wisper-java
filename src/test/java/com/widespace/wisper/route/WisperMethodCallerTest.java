package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


public class WisperMethodCallerTest
{

    @Test(expected = WisperException.class)
    public void givenWrongMessageType_ThrowsException() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:~");
        new WisperMethodCaller(mock(WisperClassModel.class), request);
    }


    @Test
    public void callDispatchessStaticMethods() throws Exception
    {
        String methodName = "methodName";
        Request request = new Request().withMethodName("whatever.whatever.thing." + methodName);

        WisperMethodCaller methodCaller = spy(new WisperMethodCaller(mock(WisperClassModel.class), request));
        methodCaller.call();
        verify(methodCaller).callStatic(any(WisperMethod.class));
    }

    @Test
    public void callDispatchesInstanceMethods() throws Exception
    {
        String methodName = "methodName";
        Request request = new Request().withMethodName("whatever.whatever.thing:" + methodName);

        WisperMethodCaller methodCaller = spy(new WisperMethodCaller(mock(WisperClassModel.class), request));
        methodCaller.call();
        verify(methodCaller).callInstance(any(WisperMethod.class));
    }

    @Test
    public void givenStaticMethodRequest_CallsStaticMethodOnTheActualClass() throws Exception
    {
 fail();
    }

    //--------------------------
    private WisperInstanceModel createInstanceAndReturnWisperInstance(String mapName) throws InterruptedException
    {
        Request creationRequest = new Request();
        creationRequest.setIdentifier("ABCD1");
        creationRequest.setMethod(mapName + "~");

        final WisperInstanceModel[] _instanceModel = new WisperInstanceModel[1];
        WisperInstanceCreator creator = new WisperInstanceCreator(RoutesTestObject.registerRpcClass(), creationRequest);
        creator.create(new RemoteInstanceCreatorCallback()
        {
            @Override
            public void result(WisperInstanceModel instanceModel, WisperException ex)
            {
                _instanceModel[0] = instanceModel;
            }
        });

        if (_instanceModel[0] == null)
            Thread.sleep(400);

        return _instanceModel[0];
    }
}