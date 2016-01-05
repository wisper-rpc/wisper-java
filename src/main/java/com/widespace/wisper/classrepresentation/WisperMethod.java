package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.base.RPCUtilities;

import java.util.Arrays;
import java.util.List;

/**
 * This class is a representation of a method in an RPC class.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public class WisperMethod
{
    private String mapName;
    private List<RPCMethodParameterType> paramTypes;
    private CallBlock callBlock;
    private String methodName;

    public WisperMethod(String mapName, String methodName, RPCMethodParameterType... parameterTypes)
    {
        this.mapName = mapName;
        this.methodName = methodName;
        this.paramTypes = Arrays.asList(parameterTypes);
    }

    public WisperMethod(String mapName, CallBlock callBlock)
    {
        this.mapName = mapName;
        this.callBlock = callBlock;
    }

    /**
     * Returns the actual name of the method
     *
     * @return actual name of the method
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * Returns the call block if any. call blocks are used as an equivalent to
     * obj c blocks to perform a piece of code
     *
     * @return the call block if any
     */
    public CallBlock getCallBlock()
    {
        return callBlock;
    }

    /**
     * Setter for the call block. call blocks are used as an equivalent to obj c
     * blocks to perform a piece of code
     *
     * @param callBlock the new call block
     */
    public void setCallBlock(CallBlock callBlock)
    {
        this.callBlock = callBlock;
    }

    /**
     * returns the types of parameters used by the actual method. Parameter
     * types could be one of the following:
     * <p/>
     * STRING - will result in Java String NUMBER - Will result in Java Number
     * ARRAY - will result in Java array which in turn could be changes into
     * ArrayList HASHMAP - will result in Java Hashmap (equivalent to Javascript
     * Object)
     *
     * @return an array containing the classes of parameter types.
     * @see RPCMethodParameterType
     */
    @SuppressWarnings("rawtypes")
    public Class[] getParameterTypes()
    {
        return RPCUtilities.convertRpcParameterTypeToClassType(paramTypes);
    }

    /**
     * Returns the mapping name of the method used in the RPC.
     *
     * @return a string representing the map name.
     */
    public String getMapName()
    {
        return mapName;
    }
}
