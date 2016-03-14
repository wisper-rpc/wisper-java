package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.ResponseBlock;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.messagetype.error.WisperException;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class WisperInstanceDestructorTest
{
    @Before
    public void setUp() throws Exception
    {
        WisperInstanceRegistry.sharedInstance().clear();
        RoutesTestObject.reset();
    }

    @Test(expected = WisperException.class)
    public void thowsExceptionOnNonDestructCalls() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:call");
        request.setIdentifier("ABCD1");
        WisperInstanceDestructor destructor = new WisperInstanceDestructor(request, mock(Router.class));
    }

    @Test
    public void acceptsDestructMessageType() throws Exception
    {
        Request request = new Request();
        request.setMethod("a.b.c:~");
        request.setIdentifier("ABCD1");
        WisperInstanceDestructor destructor = new WisperInstanceDestructor(request, mock(Router.class));
        assertThat(destructor, is(notNullValue()));
    }

    @Test
    public void acceptsDestructMessageTypeAsNotification() throws Exception
    {
        Notification notification = new Notification();
        notification.setMethodName("a.b.c:~");
        notification.setParams(new Object[]{"ABCD1"});
        WisperInstanceDestructor destructor = new WisperInstanceDestructor(notification, mock(Router.class));
        assertThat(destructor, is(notNullValue()));
    }

    @Test
    public void givenWisperInstance_CallsDestructOnThatInstance() throws Exception
    {
        String mapName = "whatever.whatever.thing";
        WisperInstanceModel wisperInstance = createInstanceAndReturnWisperInstance(mapName);
        Router router = mock(Router.class);
        WisperInstanceRegistry.sharedInstance().addInstance(wisperInstance, router.getRootRoute());

        Request destructReq = destructRequest(wisperInstance.getInstanceIdentifier());

        WisperInstanceDestructor destructor = new WisperInstanceDestructor(destructReq, router);
        RoutesTestObject subject = (RoutesTestObject) wisperInstance.getInstance();
        destructor.destroy();

        assertThat(subject.destructCalled(), is(true));
    }


    @Test(expected = WisperException.class)
    public void givenWrongInstance_WisperExceptionIsThrown() throws Exception
    {
        WisperInstanceModel wisperInstance = createInstanceAndReturnWisperInstance("whatever.map.name");
        Router router = mock(Router.class);
        WisperInstanceRegistry.sharedInstance().addInstance(wisperInstance, router);

        WisperInstanceDestructor destructor = new WisperInstanceDestructor(destructRequest("fakeId"), router);
        destructor.destroy();
    }

    @Test
    public void destructRemovesTheInstanceFromRegistry() throws Exception
    {
        WisperInstanceModel wisperInstance = createInstanceAndReturnWisperInstance("whatever.map.name");
        Router router = mock(Router.class);
        WisperInstanceRegistry.sharedInstance().addInstance(wisperInstance, router.getRootRoute());

        WisperInstanceDestructor destructor = new WisperInstanceDestructor(destructRequest(wisperInstance.getInstanceIdentifier()), router);
        destructor.destroy();

        WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceWithId(wisperInstance.getInstanceIdentifier());
        assertThat(instanceModel, is(nullValue()));
    }

    @Test
    public void destructNotificationRemovesTheInstanceFromRegistry() throws Exception
    {
        WisperInstanceModel wisperInstance = createInstanceAndReturnWisperInstance("whatever.map.name");
        Router router = new Router();
        WisperInstanceRegistry.sharedInstance().addInstance(wisperInstance, router.getRootRoute());

        Request request = destructRequest(wisperInstance.getInstanceIdentifier());
        Notification notification = new Notification().withMethodName(request.getMethodName()).withParams(new Object[]{wisperInstance.getInstanceIdentifier()});
        WisperInstanceDestructor destructor = new WisperInstanceDestructor(notification, router);
        destructor.destroy();

        WisperInstanceModel instanceModel = WisperInstanceRegistry.sharedInstance().findInstanceUnderRoute(wisperInstance.getInstanceIdentifier(), router.getRootRoute());
        assertThat(instanceModel, is(nullValue()));
    }

    @Test
    public void destructRespondsBackOnRequest() throws Exception
    {
        WisperInstanceModel wisperInstance = createInstanceAndReturnWisperInstance("whatever.map.name");
        Router router = mock(Router.class);
        WisperInstanceRegistry.sharedInstance().addInstance(wisperInstance, router.getRootRoute());
        Request request = destructRequest(wisperInstance.getInstanceIdentifier());
        final boolean[] callblockCalled = {false};
        request.setResponseBlock(new ResponseBlock()
        {
            @Override
            public void perform(Response response, RPCErrorMessage error)
            {
                callblockCalled[0] = true;
            }
        });

        WisperInstanceDestructor destructor = new WisperInstanceDestructor(request, router);
        destructor.destroy();

        assertThat(callblockCalled[0], is(true));
    }

    //--------------------------
    private WisperInstanceModel createInstanceAndReturnWisperInstance(String mapName) throws InterruptedException
    {
        Request creationRequest = new Request();
        creationRequest.setIdentifier("ABCD1");
        creationRequest.setMethod(mapName + "~");

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
            Thread.sleep(400);


        return _instanceModel[0];
    }

    @NotNull
    private Request destructRequest(String instanceId)
    {
        Request destructReq = new Request();
        destructReq.setMethod("whatever:~");
        destructReq.setParams(new Object[]{instanceId});
        return destructReq;
    }
}