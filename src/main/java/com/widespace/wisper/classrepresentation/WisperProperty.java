package com.widespace.wisper.classrepresentation;

/**
 * A model object that describes a mapped property in a WSRPCClass.
 * Properties will be automatically set when an incoming message asks to set it (if allowed by its mode).
 */
public class WisperProperty
{
    private final String mappingName;
    private final WisperPropertyAccess mode;
    private final String setterName;
    private final WisperParameterType setterMethodParameterType;

    public WisperProperty(String mappingName, WisperPropertyAccess mode, String setterName, WisperParameterType setterMethodParameterType)
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
    public WisperProperty(String mappingName)
    {
        this(mappingName, WisperPropertyAccess.READ_ONLY, null, null);
    }

    public String getMappingName()
    {
        return mappingName;
    }

    public WisperPropertyAccess getMode()
    {
        return mode;
    }

    public String getSetterName()
    {
        return setterName;
    }

    public WisperParameterType getSetterMethodParameterType()
    {
        return setterMethodParameterType;
    }
}


