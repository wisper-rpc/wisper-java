package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.WisperEventBuilder;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class RouterTest
{
    private Router router;

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
    public void givenRoutesWithSimilarStartPath_shouldNotReject() throws Exception
    {
        router.exposeRoute("a", new Router());
        router.exposeRoute("a.b", new Router());



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

    @Test
    public void exposeRoute_setsNamespacesProperly() throws Exception
    {
        Router anotherRouter = new Router();
        router.exposeRoute("stairway.to.heaven", anotherRouter);

        Router router1 = this.router.getRoutes().get("stairway");
        assertThat(router1.getNamespace(), is("stairway"));

        Router router2 = router1.getRoutes().get("to");
        assertThat(router2.getNamespace(), is("to"));

        Router router3 = router2.getRoutes().get("heaven");
        assertThat(router3.getNamespace(), is("heaven"));
    }

    @Test(expected = WisperException.class)
    public void routerWillThrowExceptionOnRouteNotFound() throws Exception
    {
        Request WRONG_REQUEST = new Request(new JSONObject("{\"id\":\"1234\", \"method\":\"a.x.y.z:a\", \"params\":[\"index_1\", \"index_2\"] }"));
        router.routeMessage(WRONG_REQUEST, "a.b.c");
    }

    @Test(expected = WisperException.class)
    public void routerWillThrowExceptionOnNullPath() throws Exception
    {
        Request WRONG_REQUEST = new Request(new JSONObject("{\"id\":\"1234\", \"method\":\"a.x.y.z:a\", \"params\":[\"index_1\", \"index_2\"] }"));
        router.routeMessage(WRONG_REQUEST, null);
    }

    @Test
    public void canAcceptParentRoute() throws Exception
    {
        Router parentRouter = new Router();
        router.setParentRoute(parentRouter);

        assertThat(router.getParentRoute(), is(notNullValue()));
        assertThat(router.getParentRoute(), is(parentRouter));
    }


    @Test
    public void canAcceptNameSpace() throws Exception
    {
        String namespace = "myNamespace";
        router = new Router(namespace);
        assertThat(router.getNamespace(), is(notNullValue()));
        assertThat(router.getNamespace(), is(namespace));
    }

    @Test
    public void givenNoNamespace_nameSpaceIsNull() throws Exception
    {
        assertThat(router.getNamespace(), is(nullValue()));
    }

    @Test
    public void givenParentRoute_canReverseRouteToParent() throws Exception
    {
        // parent <-- router <-- (outer) <= (inner) : innerRouter
        router = spy(new Router());
        Router innerRouter = spy(new Router());
        router.exposeRoute("outerPath.innerPath", innerRouter);
        router.setParentRoute(new Router("parent"));

        AbstractMessage message = new WisperEventBuilder().withName("name").withValue("something").buildStaticEvent();
        String path = null;

        innerRouter.reverseRoute(message, path);
        verify(router).reverseRoute(eq(message), eq("outerPath.innerPath"));
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

        verify(router_y).routeMessage((AbstractMessage) anyObject(), anyString());
    }

    @Test
    public void canFindRootRouter() throws Exception
    {
        Router parentRoute = new Router();
        router.setParentRoute(parentRoute);

        Router theRoot = router.getRootRoute();
        assertThat(theRoot, is(parentRoute));
    }

    @Test
    public void canFindRootRouterOnMultipleRoutes() throws Exception
    {
        Router innerRouter = new Router();
        this.router.exposeRoute("a.b.c", innerRouter);

        Router rootRoute = innerRouter.getRootRoute();
        assertThat(rootRoute, is(router));
    }

    @Test
    public void canFindRootRouterOnMultipleRoutesWithExplicitParent() throws Exception
    {
        Router innerRouter = new Router();
        router.exposeRoute("a.b.c", innerRouter);
        Router parentRouter = new Router();
        router.setParentRoute(parentRouter);

        Router rootRoute = innerRouter.getRootRoute();
        assertThat(rootRoute, is(parentRouter));
    }

}