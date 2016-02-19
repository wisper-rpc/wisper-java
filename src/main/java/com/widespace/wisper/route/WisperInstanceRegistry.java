package com.widespace.wisper.route;


import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.messagetype.error.WisperException;
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

    public WisperInstanceModel findInstanceUnderRoute(@NotNull String insanceIdentifier, @NotNull Router router)
    {
        HashMap<String, WisperInstanceModel> instancesUnderRoute = (instances != null && instances.get(router) != null) ? instances.get(router) : null;
        return (instancesUnderRoute != null) ? instancesUnderRoute.get(insanceIdentifier) : null;
    }

    public WisperInstanceModel findInstanceWithId(@NotNull String insanceIdentifier)
    {
        HashMap<Router, HashMap<String, WisperInstanceModel>> allInstances = getInstances();
        if (allInstances == null)
            return null;

        for (Router router : allInstances.keySet())
        {
            HashMap<String, WisperInstanceModel> instancesUnderRoute = WisperInstanceRegistry.sharedInstance().getInstancesUnderRoute(router);
            if (instancesUnderRoute != null && instancesUnderRoute.containsKey(insanceIdentifier))
            {
                return instancesUnderRoute.get(insanceIdentifier);
            }
        }

        return null;
    }

    public Router findRouterForInstanceId(String instanceIdentifier)
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


    public HashMap<Router, HashMap<String, WisperInstanceModel>> getInstances()
    {
        return instances;
    }

    public void setInstances(HashMap<Router, HashMap<String, WisperInstanceModel>> instances)
    {
        this.instances = instances;
    }

    public HashMap<String, WisperInstanceModel> getInstancesUnderRoute(Router router)
    {
        return instances != null && instances.containsKey(router) ? instances.get(router) : null;
    }

    public void clear()
    {
        if (instances != null)
            instances.clear();
    }


}
