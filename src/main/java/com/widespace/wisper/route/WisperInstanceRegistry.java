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

    public WisperInstanceModel findInstanceUnderRoute(String insanceIdentifier, @NotNull Router router)
    {
        HashMap<String, WisperInstanceModel> instancesUnderRoute = (instances != null && instances.get(router) != null) ? instances.get(router) : null;
        return (instancesUnderRoute != null) ? instancesUnderRoute.get(insanceIdentifier) : null;
    }

    public WisperInstanceModel findInstanceWithId(String instanceIdentifier)
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


    public boolean removeInstance(String instanceIdentifier)
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
