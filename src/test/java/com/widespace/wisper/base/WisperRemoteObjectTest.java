package com.widespace.wisper.base;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.*;
import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import com.widespace.wisper.route.EventRouter;
import com.widespace.wisper.route.GatewayRouter;
import com.widespace.wisper.route.Router;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class WisperRemoteObjectTest
{

    @Test
    public void testInstanceIdentifier() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        assertThat(obj.getInstanceIdentifier(), is(equalTo(null)));

        obj.setInstanceIdentifier("foo1");

        assertThat(obj.getInstanceIdentifier(), is(equalTo("foo1")));
    }

    @Test
    public void testCreateInstanceSendsCreateMessage() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);;

        // Assert that only create message has been sent
        assertThat(router.messages.size(), is(equalTo(1)));

        Request createRequest = (Request)router.messages.get(0);

        assertThat(createRequest.getMethodName(), is(equalTo("Foo~")));
        assertThat(createRequest.getParams(), is(equalTo(new Object[]{})));
    }

    @Test
    public void testCreateInstanceResponseRegistersInstanceWithEventRouter() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);;

        obj.setInstanceIdentifier("mockedInstanceId0x00");

        EventRouter eventRouter = (EventRouter) router.getRoutes().get("Foo");

        assertThat(eventRouter.getRemoteObjects().size(), is(1));
        assertThat((WisperRemoteObject)eventRouter.getRemoteObjects().get("mockedInstanceId0x00"), is(equalTo(obj)));
    }

    @Test
    public void testDestroyInstanceSendsDestroyMessage() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);;

        obj.setInstanceIdentifier("mockedInstanceId0x00");
        obj.destroy();

        // Assert that only create message has been sent
        assertThat(router.messages.size(), is(equalTo(2)));

        Notification destroyNotification = (Notification) router.messages.get(1);

        System.out.println("ASSDASDASD");
        System.out.println(destroyNotification.getMethodName());

        assertThat(destroyNotification.getMethodName(), is(equalTo("Foo:~")));
        assertThat(destroyNotification.getParams().length, is(1));
        assertThat((String)destroyNotification.getParams()[0], is(equalTo("mockedInstanceId0x00")));
    }

    @Test
    public void testDestroyInstanceUnregistersInstanceFromEventRouter() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);;

        obj.setInstanceIdentifier("mockedInstanceId0x00");
        obj.destroy();

        EventRouter eventRouter = (EventRouter) router.getRoutes().get("Foo");

        assertThat(eventRouter.getRemoteObjects().size(), is(0));
    }

    @Test
    public void testRouteStaticEventToInstance() throws Exception
    {
        GatewayRouter router = new GatewayRouter();
        MyWisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        //Create static event
        Notification eventNotification = new Notification("Foo!", new Object[]{"volume", 1.0});

        //Route event
        router.gatewayReceivedMessage(eventNotification);

        assertThat(obj.recievedStaticEvent.getName(), is("volume"));
    }

    @Test
    public void testRouteInstanceEventToInstance() throws Exception
    {
        GatewayRouter router = new GatewayRouter();
        MyWisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        obj.setInstanceIdentifier("mockedInstanceID0x00");

        //Create static event
        Notification eventNotification = new Notification("Foo:!", new Object[]{"mockedInstanceID0x00", "volume", 1.0});

        //Route event
        router.gatewayReceivedMessage(eventNotification);

        assertThat(obj.recievedEvent.getName(), is("volume"));
    }

    @Test
    public void testCallInstanceMethod() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        // Call a method before instance identifier has been set
        obj.callInstanceMethod("bar", new Object[]{1, 2, 3});

        // Assert that only create message has been sent
        assertThat(router.messages.size(), is(equalTo(1)));

        // Simulate a create response by setting the instance identifier
        obj.setInstanceIdentifier("foo2");

        Notification expected = new Notification("Foo:bar", new Object[]{"foo2", 1, 2, 3});

        // Assert a message has been sent and that it equals the expected one
        assertThat(router.messages.size(), is(equalTo(2)));
        assertThat((Notification) router.messages.get(1), is(equalTo(expected)));

        // Call the method a second time
        obj.callInstanceMethod("bar", new Object[]{1, 2, 3});


        // Assert that another message is sent immediately
        assertThat(router.messages.size(), is(equalTo(3)));
        assertThat((Notification) router.messages.get(2), is(equalTo(expected)));
    }

    @Test
    public void testCallStaticMethod() throws Exception
    {
        StubbedGatewayRouter gateway = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", gateway);

        obj.callStaticMethod("foo", new Object[]{1});

        obj.callStaticMethod("foo", new Object[]{1}, new CompletionBlock()
        {
            @Override
            public void perform(@Nullable Object result, @Nullable RPCErrorMessage error)
            {
                // Do nothing
            }
        });

        // First messages is create, second message is Notification because no response block, third is Request
        // Just call the response block to see that we get complete code coverage
        ((Request)gateway.messages.get(2)).getResponseBlock().perform(new Response(), null);
    }

    @Test
    public void testSendInstanceEvent() throws Exception
    {
        StubbedGatewayRouter channel = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", channel);

        obj.sendInstanceEvent("baz", 1);

        // Assert that only create message has been sent
        assertThat(channel.messages.size(), is(equalTo(1)));

        // Set the identifier
        obj.setInstanceIdentifier("foo2");

        Event expected = new Event("Foo:!", "foo2", "baz", 1);

        assertThat(channel.messages.size(), is(equalTo(2)));
        assertThat((Event) channel.messages.get(1), is(equalTo(expected)));

        obj.sendInstanceEvent("baz", 1);

        assertThat(channel.messages.size(), is(equalTo(3)));
        assertThat((Event) channel.messages.get(2), is(equalTo(expected)));

    }

    @Test
    public void testSendStaticEvent() throws Exception
    {
        StubbedGatewayRouter router = new StubbedGatewayRouter();
        WisperRemoteObject obj = new MyWisperRemoteObject("Foo", router);

        obj.sendStaticEvent("create", null);

        Event expected = new Event("Foo!", "create", null);
        assertThat(router.messages.size(), is(equalTo(2)));
        assertThat((Event) router.messages.get(1), is(equalTo(expected)));
    }


    @Test
    public void testWhenCreatedGatewayRouter_canRouteMapName() throws Exception
    {
        StubbedGatewayRouter gatewayRouter = new StubbedGatewayRouter();
        WisperRemoteObject object = new MyWisperRemoteObject("whatever.some.thing", gatewayRouter);

        assertThat(gatewayRouter.getRoutes().containsKey("whatever"), is(true));
        Router router1 = gatewayRouter.getRoutes().get("whatever");

        assertThat(router1.getRoutes().containsKey("some"), is(true));
        Router router2 = router1.getRoutes().get("some");

        assertThat(router2.getRoutes().containsKey("thing"), is(true));

        assertThat(gatewayRouter.hasRoute("whatever.some.thing"), is(true));
    }

    @Test
    public void testWhenRemoteObjectCreated_EventRouterIsAddedToGatewayRouter() throws Exception
    {
        StubbedGatewayRouter gatewayRouter = new StubbedGatewayRouter();
        WisperRemoteObject object = new MyWisperRemoteObject("a.b.c", gatewayRouter);

        assertThat(gatewayRouter.hasRoute("a.b.c"), is(true));

        Router finalRouter = gatewayRouter.getRoutes().get("a").getRoutes().get("b").getRoutes().get("c");
        assertThat(finalRouter, is(instanceOf(EventRouter.class)));
    }

    //region Remote Object Events
    //==================================================================

    @Test
    public void testWisperRemoteObject_handlesInstanceEvent() throws Exception
    {
        String instanceId = "testIdentifier";
        StubbedGatewayRouter stubbedGatewayRouter = new StubbedGatewayRouter();
        MyWisperRemoteObject remoteObject = new MyWisperRemoteObject("a.b.c",stubbedGatewayRouter);
        remoteObject.setInstanceIdentifier(instanceId);
        Event instanceEvent = new Event("a.b.c:!", instanceId, "name", "value");
        stubbedGatewayRouter.routeMessage(instanceEvent,"a.b.c");

        assertThat(remoteObject.recievedEvent, is(notNullValue()));
        assertThat(remoteObject.recievedEvent.getIdentifier(),is(equalTo(instanceEvent.getIdentifier())));
        assertThat(remoteObject.recievedEvent.getInstanceIdentifier(),is(equalTo(instanceEvent.getInstanceIdentifier())));
    }


    //region Utilities
    //==================================================================


    private class StubbedGatewayRouter extends GatewayRouter
    {
        public List<AbstractMessage> messages = new ArrayList<AbstractMessage>();
        public Gateway gateway = new Gateway(mock(GatewayCallback.class))
        {
            @Override
            public void sendMessage(AbstractMessage message)
            {
                messages.add(message);
            }
        };

        @Override
        public Gateway getGateway()
        {
            return gateway;
        }
    }

    private class DoNothingChannel implements Channel
    {
        @Override
        public void sendMessage(AbstractMessage message)
        {
            // do nothing
        }
    }

}
