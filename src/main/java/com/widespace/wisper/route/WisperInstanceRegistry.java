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
