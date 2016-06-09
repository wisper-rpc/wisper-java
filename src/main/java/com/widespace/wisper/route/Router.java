package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Router
{
    private HashMap<String, Router> routes;
    private Router parentRoute;

    private String namespace;

    public Router()
    {
        routes = new HashMap<String, Router>();
    }

    public Router(String namespace)
    {
        this();
        this.namespace = namespace;
    }

    /**
     * Tries to route a message to a given path. If the path is not routable, a ROUTE_NOT_FOUND exception
     * is thrown.
     *
     * @param message the message to be routed.
     * @param path    the path the message is supposed to be routed to.
     * @throws WisperException when no route is found for the given path.
     */
    public void routeMessage(CallMessage message, String path) throws WisperException
    {
        if (path == null)
            throw new WisperException(Error.ROUTE_NOT_FOUND, null, "Route was null on message : " + message.toJsonString());

        List<String> tokens = new ArrayList<String>(Arrays.asList(path.split("\\.")));
        String firstChunk = tokens.get(0);
        String remainingPath = getRemainingPath(path, firstChunk);
        if (firstChunk.equals(path))
            firstChunk = firstChunk.split(":")[0];

        firstChunk = getPathUntilSpecialMarkers(firstChunk);
        checkPathExists(message, firstChunk);
        routes.get(firstChunk).routeMessage(message, remainingPath);
    }

    private String getPathUntilSpecialMarkers(String path)
    {
        String result = path.split(":")[0];
        result = result.split("~")[0];
        result = result.split("!")[0];
        return result;
    }

    private String getRemainingPath(String path, String firstChunk)
    {
        String result = path.equals(firstChunk) ? "" : path.substring(firstChunk.length() + 1);
        String[] split = result.split(":");
        result = split[0];
        result = result.replace("~", "");
        result = result.replace("!", "");
        return result;
    }


    private void checkPathExists(CallMessage message, String path)
    {
        if (routes == null || path == null || !routes.containsKey(path))
        {
            throw new WisperException(Error.ROUTE_NOT_FOUND, null, "No route found for " + path + " on message : " + message.toJsonString());
        }
    }


    /**
     * Exposes a path on this router with the given router.
     *
     * @param path   the path. (for example "wisp.ai.Audio"). Cannot be null.
     * @param router the router to handle the path. Cannot be null.
     */
    public void exposeRoute(@NotNull String path, @NotNull Router router) throws WisperException
    {
        List<String> tokens = Arrays.asList(path.split("\\."));
        String firstChunk = tokens.get(0);
        String remainingPath = getRemainingPath(path, firstChunk);

        rejectAlreadyExistingRoute(firstChunk, remainingPath);

        if (finalChunkAddedToRoutes(router, firstChunk, remainingPath))
            return;

        addRouterForNextChunk(router, firstChunk, remainingPath);
    }

    private boolean finalChunkAddedToRoutes(@NotNull Router router, String firstChunk, String remainingPath)
    {
        if (remainingPath.isEmpty())
        {
            routes.put(firstChunk, router);
            router.setParentRoute(this);
            router.namespace = firstChunk;
            return true;
        }
        return false;
    }

    private void addRouterForNextChunk(@NotNull Router router, String firstChunk, String remainingPath)
    {
        if (!routes.containsKey(firstChunk))
        {
            Router newRouter = new Router();
            newRouter.setParentRoute(this);
            routes.put(firstChunk, newRouter);
            newRouter.namespace = firstChunk;

            newRouter.exposeRoute(remainingPath, router);

        } else
        {
            routes.get(firstChunk).exposeRoute(remainingPath, router);
        }
    }

    private void rejectAlreadyExistingRoute(@NotNull String path, String remainingPath)
    {
        if (remainingPath.isEmpty() && routes.containsKey(path))
        {
            throw new WisperException(Error.ROUTE_ALREADY_EXISTS, null, "A route already exists on the router for path \"" + path + "\"");
        }
    }

    public boolean hasRoute(String path)
    {
        List<String> tokens = Arrays.asList(path.split("\\."));
        String firstChunk = tokens.get(0);
        String remainingPath = getRemainingPath(path, firstChunk);

        if (routes == null || !routes.containsKey(firstChunk))
            return false;

        if ((remainingPath == null || remainingPath.isEmpty()) && routes.containsKey(firstChunk))
            return true;

        return (routes.get(firstChunk)).hasRoute(remainingPath);
    }

    public HashMap<String, Router> getRoutes()
    {
        return routes;
    }

    public Router getParentRoute()
    {
        return parentRoute;
    }

    public void setParentRoute(Router parentRoute)
    {
        this.parentRoute = parentRoute;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public void reverseRoute(@NotNull AbstractMessage message, @Nullable String path)
    {
        String newPath = (path == null) ? namespace : namespace + "." + path;
        if (parentRoute != null)
            parentRoute.reverseRoute(message, newPath);
    }

    /**
     * Returns the router of the given path, or null if the path is not found.
     * Works both on single path and composite paths. e.g. "a" or "a.b.c".
     *
     * @param path a dot-separated string representing the path.
     * @return the Router, or null.
     */
    public Router getRouter(@NotNull String path)
    {
        List<String> tokens = Arrays.asList(path.split("\\."));
        String firstChunk = tokens.get(0);
        String remainingPath = getRemainingPath(path, firstChunk);
        if (remainingPath.isEmpty())
        {
            if (routes == null || !routes.containsKey(firstChunk))
                return null;

            return routes.get(firstChunk);
        }

        return routes.get(firstChunk).getRouter(remainingPath);
    }

    /**
     * Returns the root route of this router. A root root is the uppermost parent.
     * If there is no parent assigned, the router itself is the root.
     *
     * @return a Router.
     */
    @NotNull
    public Router getRootRoute()
    {
        if (this.parentRoute != null)
            return parentRoute.getRootRoute();

        return this;
    }


}
