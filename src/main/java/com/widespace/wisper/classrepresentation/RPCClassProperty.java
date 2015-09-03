package com.widespace.wisper.classrepresentation;

/**
 * A model object that describes a mapped property in a WSRPCClass.
 * Properties will be automatically set when an incoming message asks to set it (if allowed by its mode).
 */
public class RPCClassProperty
{
    private final String mappingName;
    private final RPCClassPropertyMode mode;
    private final String setterName;
    private final RPCMethodParameterType setterMethodParameterType;

    public RPCClassProperty(String mappingName, RPCClassPropertyMode mode, String setterName, RPCMethodParameterType setterMethodParameterType)
    {
        this.mappingName = mappingName;
        this.mode = mode;
        this.setterName = setterName;
        this.setterMethodParameterType = setterMethodParameterType;
    }

    /**
     * Creates a read only property
     *
     * @param mappingName
     */
    public RPCClassProperty(String mappingName)
    {
        this(mappingName, RPCClassPropertyMode.READ_ONLY, null, null);
    }

    public String getMappingName()
    {
        return mappingName;
    }

    public RPCClassPropertyMode getMode()
    {
        return mode;
    }

    public String getSetterName()
    {
        return setterName;
    }

    public RPCMethodParameterType getSetterMethodParameterType()
    {
        return setterMethodParameterType;
    }
}


