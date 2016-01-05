package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.base.Wisper;

/**
 * This is a model object containing the instance of an RPC object. The actual instance class has to
 * implement Wisper and has a static method registerClass().
 * <p/>
 * Created by Ehssan Hoorvash on 23/05/14.
 */
public class WisperInstanceModel
{
    private WisperClassModel wisperClassModel;
    private String instanceIdentifier;
    private Wisper instance;


    public WisperInstanceModel(WisperClassModel wisperClassModel, Wisper instance, String instanceIdentifier)
    {
        this.setWisperClassModel(wisperClassModel);
        this.instanceIdentifier = instanceIdentifier;
        this.instance = instance;
    }

    /**
     * returns the unique identifier of this rpc instance
     *
     * @return a string representing the identifier
     */
    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    /**
     * returns the actual Java instance of the object. The instance has to implement Wisper
     * and has a static method registerClass().
     *
     * @return the instance
     * @see Wisper
     */
    public Wisper getInstance()
    {
        return instance;
    }

    public WisperClassModel getWisperClassModel()
    {
        return wisperClassModel;
    }

    public void setWisperClassModel(WisperClassModel wisperClassModel)
    {
        this.wisperClassModel = wisperClassModel;
    }
}
