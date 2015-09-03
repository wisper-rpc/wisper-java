package com.widespace.wisper.messagetype.error;

/**
 * Created by Ehssan Hoorvash on 04/06/14.
 */
public enum RemoteObjectErrorCode
{
    MISSING_CLASS_ERROR(0, "MissingClassError"),
    INVALID_INSTANCE_ERROR(1, "InvalidInstanceError"),
    MISSING_METHOD_ERROR(2, "MissingProcedureError"),
    INVALID_ARGUMENTS_ERROR(3, "InvalidArgumentsError");

    private int errorCode;
    private String errorName;

    RemoteObjectErrorCode(int errorCode, String errorName)
    {
        this.errorCode = errorCode;
        this.errorName = errorName;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public String getErrorName()
    {
        return errorName;
    }
}