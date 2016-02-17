package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Router
{
    private HashMap<String, Router> routes;
    private Router parentRoute;

    public Router()
    {
        routes = new HashMap<String, Router>();
    }

    public void routeMessage(AbstractMessage message, String path) throws WisperException
    {
        if (path == null)
            throw new WisperException(Error.ROUTE_NOT_FOUND, null, "Route was null on message : " + message.toJsonString());

        List<String> tokens = new ArrayList<String>(Arrays.asList(path.split("\\.")));
        String firstChunk = tokens.get(0);
        String remainingPath = path.equals(firstChunk) ? "" : path.substring(firstChunk.length() + 1);
        checkPathExists(message, firstChunk);
        routes.get(firstChunk).routeMessage(message, remainingPath);
    }


    private void checkPathExists(AbstractMessage message, String path)
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
        String remainingPath = path.equals(firstChunk) ? "" : path.substring(firstChunk.length() + 1);

        rejectAlreadyExistingRoute(firstChunk);

        if (finalChunkAddedToRoutes(router, firstChunk, remainingPath))
            return;

        routeNextChunks(router, firstChunk, remainingPath);
    }

    private boolean finalChunkAddedToRoutes(@NotNull Router router, String firstChunk, String remainingPath)
    {
        if (remainingPath.isEmpty())
        {
            routes.put(firstChunk, router);
            router.setParentRoute(this);
            return true;
        }
        return false;
    }

    private void routeNextChunks(@NotNull Router router, String firstChunk, String remainingPath)
    {
        Router newRouter = new Router();
        newRouter.setParentRoute(this);
        routes.put(firstChunk, newRouter);

        newRouter.exposeRoute(remainingPath, router);
    }

    private void rejectAlreadyExistingRoute(@NotNull String path)
    {
        if (routes.containsKey(path))
        {
            throw new WisperException(Error.ROUTE_ALREADY_EXISTS, null, "A route already exists on the router for path " + path);
        }
    }

    public boolean hasRoute(String route)
    {
        return routes != null && routes.containsKey(route);
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
}
