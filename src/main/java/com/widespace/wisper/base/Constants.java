package com.widespace.wisper.base;

/**
 * Fields that are used in Wisper messages
 */
public enum Constants
{
    //----------------------------------------
    //region Message Fields
    //----------------------------------------
    ID("id"),
    METHOD("method"),
    PARAMS("params"),
    RESULT("result"),
    ERROR("error"),
    PROPERTIES("props"),

    //----------------------------------------
    //region Protocol Fields
    //----------------------------------------
    CONSTRUCTOR_TOKEN("~");


    private String value;

    Constants(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
