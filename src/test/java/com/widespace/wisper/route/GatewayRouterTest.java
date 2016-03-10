package com.widespace.wisper.route;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class GatewayRouterTest
{
    private GatewayRouter gatewayRouter;
    private Gateway gateway;

    @Before
    public void setUp() throws Exception
    {
        GatewayCallback callBackMock = mock(GatewayCallback.class);
        gateway = new Gateway(callBackMock);
        gatewayRouter = new GatewayRouter(gateway);
    }

    @Test
    public void testGatewayRouterForwardsIncomingMessages() throws Exception
    {
        Request request = new Request();
        request.setIdentifier("ABCD1");
        String methodName = "a.b.c:call";
        request.setMethod(methodName);
        Router routerMock = mock(Router.class);
        gatewayRouter.exposeRoute(methodName, routerMock);

        gateway.handleMessage(request);

        verify(routerMock).routeMessage(eq(request), anyString());
    }

    @Test
    public void canRegisterClassesOnGateway() throws Exception
    {
        gatewayRouter.register(RoutesTestObject.class, "a.b.c");
        assertThat(gatewayRouter.getRoutes(), is(notNullValue()));
        assertThat(gatewayRouter.getRoutes().containsKey("a"), is(true));
        assertThat(gatewayRouter.getRoutes().get("a"), is(instanceOf(Router.class)));
    }

    @Test
    public void canHandleCreateMessages() throws Exception
    {
        Request request = new Request();
        request.setIdentifier("ABCD1");
        String methodName = "a.b.c~";
        request.setMethod(methodName);

        gatewayRouter.register(RoutesTestObject.class, "a.b.c");
        gateway.handleMessage(request);

        //exception will be thrown if path is not registered.
    }

    @Test
    public void reverseRouteSendsMessagesToGateway() throws Exception
    {
        Gateway gatewayMock = mock(Gateway.class);
        gatewayRouter = new GatewayRouter(gatewayMock);

        gatewayRouter.register(RoutesTestObject.class, "a.b.c");
        AbstractMessage someMessage = mock(AbstractMessage.class);
        gatewayRouter.reverseRoute(someMessage, null);

        verify(gatewayMock).sendMessage(someMessage);
    }

    @Test
    public void givenEvent_reverseRoutePassesTheSameEventOn() throws Exception
    {
        Gateway gatewayMock = mock(Gateway.class);
        gatewayRouter = new GatewayRouter(gatewayMock);

        Event event = new WisperEventBuilder().withName("aName").withValue("aValue").withInstanceIdentifier("id").buildInstanceEvent();
        gatewayRouter.reverseRoute(event, null);

        ArgumentCaptor<AbstractMessage> captor = ArgumentCaptor.forClass(AbstractMessage.class);
        verify(gatewayMock).sendMessage(captor.capture());
        AbstractMessage preparedMessage = captor.getValue();

        assertThat(preparedMessage, is(notNullValue()));
        assertThat(preparedMessage, instanceOf(Notification.class));
        assertThat(preparedMessage, instanceOf(Event.class));
    }

    @Test
    public void givenEvent_reverseRouteChangesTheMethodNameAppropriately() throws Exception
    {
        Gateway gatewayMock = mock(Gateway.class);
        gatewayRouter = new GatewayRouter(gatewayMock);

        Event event = new WisperEventBuilder().withName("aName").withValue("aValue").withInstanceIdentifier("id").buildInstanceEvent();
        gatewayRouter.reverseRoute(event, "a.b.c");

        ArgumentCaptor<AbstractMessage> captor = ArgumentCaptor.forClass(AbstractMessage.class);
        verify(gatewayMock).sendMessage(captor.capture());
        Event preparedMessage = (Event) captor.getValue();

        String expectedMethodName= "a.b.c:!";
        assertThat(preparedMessage.getMethodName(), is(expectedMethodName));
    }


}