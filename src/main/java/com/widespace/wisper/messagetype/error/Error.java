package com.widespace.wisper.messagetype.error;

/**
 *
 */
public enum Error
{
    UNKNOWN_ERROR(-1, "An unknown error has occurred"),

    //=============================================================
    //region Messages
    //=============================================================
    PARSE_ERROR(101, "Parse error"),
    FORMAT_ERROR(102, "Invalid message format"),
    UNEXPECTED_TYPE_ERROR(103, "Message type is wrong"),
    NOT_ALLOWED(105, "The operation is not allowed"),

    //=============================================================
    //region Wisper Properties
    //=============================================================
    PROPERTY_NOT_REGISTERED(402, "Property not registered"),
    SETTER_METHOD_NOT_FOUND(403, "Setter method name not found"),
    PROPERTY_NOT_ACCESSIBLE(404, "Property not accessible"),

    SETTER_METHOD_NOT_ACCESSIBLE(405, "Setter method not accessible"),
    SETTER_METHOD_INVOCATION_ERROR(405, "Setter method could not be invoked"),
    SETTER_METHOD_WRONG_ARGUMENTS(405, "Setter method argument type error"),

    GETTER_METHOD_NOT_FOUND(406, "Getter method not found"),
    GETTER_METHOD_NOT_ACCESSIBLE(406, "Getter not accessible"),
    GETTER_METHOD_INVOCATION_ERROR(406, "Getter method invocation error"),

    //=============================================================
    //region Wisper Classes
    //=============================================================
    ROUTE_NOT_FOUND(202, "Route not found or class not registered"),
    WISPER_INSTANCE_INVALID(203, "Invalid instance."),
    NATIVE_CLASS_NOT_FOUND(204, "Native class not found"),
    CLASS_NOT_WISPER_COMPATIBLE(205, "Class is not Wisper compliant."),

    INSTANTIATION_ERROR(501, "instantiation error when constructing"),
    CONSTRUCTOR_NOT_FOUND(502, "Constructor not found"),
    CONSTRUCTOR_NOT_ACCESSIBLE(503, "Constructor not accessible"),
    CONSTRUCTOR_NOT_INVOKED(504, "Constructor invocation error"),

    //=============================================================
    //region Wisper Routes
    //=============================================================
    ROUTE_ALREADY_EXISTS(601, "Route already exists for the path"),


    //=============================================================
    //region Wisper Methods
    //=============================================================
    METHOD_NOT_REGISTERED(302, "Method not registered"),
    METHOD_NOT_ACCESSIBLE(303, "Method not accessible"),
    METHOD_NOT_FOUND(304, "Method not found"),
    METHOD_INVALID_ARGUMENTS(305, "Method arguments not matching"),
    METHOD_INVOCATION_ERROR(306, "Method not invoked");

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
