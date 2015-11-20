package com.widespace.wisper.messagetype.error;

/**
 *
 */
public enum Error
{
    UNKNOWN_ERROR(-1, "An unknown error has occurred"),

    //=============================================================
    //region Wisper Properties
    //=============================================================
    NO_SUCH_PROPERTY(111, "Property not defined"),
    PROPERTY_NOT_ACCESSIBLE(112, "Property not accessible"),
    SETTER_METHOD_NOT_FOUND(113, "Setter method name not found"),
    SETTER_METHOD_NOT_ACCESSIBLE(114, "Setter method not accessible"),
    SETTER_METHOD_INVOCATION_ERROR(115, "Setter method could not be invoked"),
    SETTER_METHOD_WRONG_ARGUMENTS(116, "Setter method argument type error."),

    //=============================================================
    //region Wisper Classes
    //=============================================================
    UNREGISTERED_WISPER_CLASS(201, "Class not registered"),
    INSTANTIATION_ERROR(202, "instantiation error when constructing."),
    CLASS_NOT_FOUND(203, "Native class not found"),
    CONSTRUCTOR_NOT_FOUND(204, "Constructor not found"),
    CONSTRUCTOR_NOT_ACCESSIBLE(205, "Constructor not accessible"),
    CONSTRUCTOR_NOT_INVOKED(206, "Constructor invocation error");


    private final int code;
    private final String description;


    Error(int code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public int getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }
}
