package com.widespace.wisper.messagetype.error;

/**
 * Created by Ehssan Hoorvash on 04/06/14.
 */
public enum RPCErrorCodes
{
    GENERIC_ERROR(0, "GenericError"),
    PARSE_ERROR(1, "ParseError"),
    FORMAT_ERROR(2, "FormatError"),
    MISSING_PROCEDURE_ERROR(3, "MissingProcedureError"),
    INVALID_MESSAGE_TYPE_ERROR(4, "InvalidMessageTypeError");

    private int errorCode;
    private String errorName;

    RPCErrorCodes(int errorCode, String errorName)
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