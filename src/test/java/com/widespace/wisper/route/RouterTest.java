package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RouterTest
{
    private Router router;
    private final static Request WRONG_REQUEST = new Request(new JSONObject("{\"id\":\"1234\", \"method\":\"a.x.y.z:a\", \"params\":[\"index_1\", \"index_2\"] }"));

    @Before
    public void setUp() throws Exception
    {
        router = new Router();
    }

    @After
    public void tearDown() throws Exception
    {
        router = null;
    }

    @Test
    public void exposeRouteWorksForSingleRoute() throws Exception
    {
        router.exposeRoute("y", new Router());
        assertThat(router.hasRoute("y"), is(true));
    }

    @Test(expected = WisperException.class)
    public void doubleRouteForSamePathNotAccepted() throws Exception
    {
        router.exposeRoute("aPath", new Router());
        router.exposeRoute("aPath", new Router()); //throws Exception
    }

    @Test
    public void exposeRouteWorksForMultipleRoutes() throws Exception
    {
        Router anotherRouter = new Router();
        router.exposeRoute("stairway.to.heaven", anotherRouter);

        assertThat(router.hasRoute("stairway"), is(true));

        Router router1 = router.getRoutes().get("stairway");
        assertThat(router1.hasRoute("to"), is(true));

        Router router2 = router1.getRoutes().get("to");
        assertThat(router2.hasRoute("heaven"), is(true));
    }

    @Test(expected = WisperException.class)
    public void routerWillThrowExceptionOnRouteNotFound() throws Exception
    {
        router.routeMessage(WRONG_REQUEST, "a.b.c");
    }

    @Test(expected = WisperException.class)
    public void routerWillThrowExceptionOnNullPath() throws Exception
    {
        router.routeMessage(WRONG_REQUEST, null);
    }

    @Test
    public void routerWillAcceptAndRouteMessages() throws Exception
    {
        Router router_x = new Router();
        Router router_y = mock(Router.class);
        Notification notificaton = new Notification("x.y.method", new String[]{"first", "second"});
        router.exposeRoute("x", router_x);
        router_x.exposeRoute("y", router_y);

        router.routeMessage(notificaton, notificaton.getMethodName());

        verify(router_y).routeMessage((AbstractMessage) anyObject(),anyString());
    }
}