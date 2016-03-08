package com.widespace.wisper.route;


import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class WisperInstanceRegistry
{
    private static WisperInstanceRegistry singletonInstance;

    private HashMap<Router, HashMap<String, WisperInstanceModel>> instances;

    /**
     * Singleton method.
     *
     * @return singletonInstance
     */
    public static WisperInstanceRegistry sharedInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new WisperInstanceRegistry();
        }

        return singletonInstance;
    }


    /**
     * Adds a wisper instance under a router. Neither the router nor the instance could be null.
     *
     * @param instanceModel the wisper instance model representing an actual instance.
     * @param router        the router.
     */
    public void addInstance(@NotNull WisperInstanceModel instanceModel, @NotNull Router router)
    {
        if (instances == null)
            instances = new HashMap<Router, HashMap<String, WisperInstanceModel>>();

        if (!instances.containsKey(router))
        {
            instances.put(router, new HashMap<String, WisperInstanceModel>());
        }

        HashMap<String, WisperInstanceModel> instancesUnderRouter = instances.get(router);
        instancesUnderRouter.put(instanceModel.getInstanceIdentifier(), instanceModel);
    }

    /**
     * Finds an inatance using its identifier under a certain route. If no such instance is found, null is returned.
     *
     * @param insanceIdentifier instance identifier of the actual instance.
     * @param router            router
     * @return the instance model for the instance if it exists, otherwise null.
     */
    public WisperInstanceModel findInstanceUnderRoute(@NotNull String insanceIdentifier, @NotNull Router router)
    {
        HashMap<String, WisperInstanceModel> instancesUnderRoute = (instances != null && instances.get(router) != null) ? instances.get(router) : null;
        return (instancesUnderRoute != null) ? instancesUnderRoute.get(insanceIdentifier) : null;
    }

    /**
     * Finds an instance with instance identifier searching all the routes.
     *
     * @param instanceIdentifier the instance identifier of the instance.
     * @return the instance model if exists, otherwise null.
     */
    public WisperInstanceModel findInstanceWithId(@NotNull String instanceIdentifier)
    {
        HashMap<Router, HashMap<String, WisperInstanceModel>> allInstances = getInstances();
        if (allInstances == null || instanceIdentifier == null)
            return null;

        for (Router router : allInstances.keySet())
        {
            HashMap<String, WisperInstanceModel> instancesUnderRoute = WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(router);
            if (instancesUnderRoute != null && instancesUnderRoute.containsKey(instanceIdentifier))
            {
                return instancesUnderRoute.get(instanceIdentifier);
            }
        }

        return null;
    }

    /**
     * Finds the route under which a certain instance is registered.
     *
     * @param instanceIdentifier the instance identifier of the actual instance.
     * @return the route if exists, otherwise null.
     */
    public Router findRouterForInstanceId(@NotNull String instanceIdentifier)
    {
        if (instances == null)
            return null;

        for (Router router : instances.keySet())
        {
            if (findInstanceUnderRoute(instanceIdentifier, router) != null)
                return router;
        }

        return null;
    }


    /**
     * Returns all the instances registered.
     *
     * @return a HashMap<Router, HashMap<String, WisperInstanceModel>> representing all the registered instance
     */
    public HashMap<Router, HashMap<String, WisperInstanceModel>> getInstances()
    {
        return instances;
    }


    /**
     * Returns all the instances registered under a certain route.
     * If no instance is registered under that woute or if the route does not exist, null is returned.
     *
     * @param router the route under which the instances
     * @return HashMap<String,WisperInstanceModel> representing all the instances, or null.
     */
    public HashMap<String, WisperInstanceModel> getInstancesUnderRoute(@NotNull Router router)
    {
        return instances != null && instances.containsKey(router) ? instances.get(router) : null;
    }

    public void clear()
    {
        if (instances != null)
            instances.clear();
    }


    /**
     * Tries to remove a certain instance using its identifier.
     * If there are no instances, or if the instance is not found under any routes, false is returned.
     * If the instance is found and removed, true is returned.
     *
     * @param instanceIdentifier removes the instance.
     * @return true if the instance removed, false otherwise.
     */
    public boolean removeInstance(@NotNull String instanceIdentifier)
    {
        HashMap<Router, HashMap<String, WisperInstanceModel>> allInstances = getInstances();
        if (allInstances == null)
            return false;

        for (Router router : allInstances.keySet())
        {
            if (instances.get(router).containsKey(instanceIdentifier))
            {
                instances.get(router).remove(instanceIdentifier);
                return true;
            }
        }

        return false;
    }
}
