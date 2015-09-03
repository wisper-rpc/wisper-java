package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.base.RPCProtocol;

/**
 * This is a model object containing the instance of an RPC object. The actual instance class has to
 * implement RPCProtocol and has a static method registerClass().
 * <p/>
 * Created by Ehssan Hoorvash on 23/05/14.
 */
public class RPCClassInstance
{
    private RPCClass rpcClass;
    private String instanceIdentifier;
    private RPCProtocol instance;


    public RPCClassInstance(RPCClass rpcClass, RPCProtocol instance, String instanceIdentifier)
    {
        this.setRpcClass(rpcClass);
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
     * returns the actual Java instance of the object. The instance has to implement RPCProtocol
     * and has a static method registerClass().
     *
     * @return the instance
     * @see com.widespace.wisper.base.RPCProtocol
     */
    public RPCProtocol getInstance()
    {
        return instance;
    }

    public RPCClass getRpcClass()
    {
        return rpcClass;
    }

    public void setRpcClass(RPCClass rpcClass)
    {
        this.rpcClass = rpcClass;
    }
}
