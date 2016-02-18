package com.widespace.wisper.route;

import com.widespace.wisper.controller.Gateway;
import com.widespace.wisper.controller.GatewayCallback;
import com.widespace.wisper.messagetype.Request;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
        Request request = new Request();
        request.setIdentifier("ABCD1");
        String methodName = "a.b.c";
        request.setMethod(methodName);

        gatewayRouter.register("a.b.c", RoutesTestObject.class);
        gateway.handleMessage(request);

        //exception will be thrown if path is not registered.
    }
}