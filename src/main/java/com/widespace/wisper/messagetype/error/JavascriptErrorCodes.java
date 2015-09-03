package com.widespace.wisper.messagetype.error;

/**
 * Created by Ehssan Hoorvash on 04/06/14.
 */
public enum JavascriptErrorCodes
{
    GENERIC_ERROR(0),
    EVALUATION_ERROR(1),
    RANGE_ERROR(2),
    REFERENCE_ERROR(3),
    SYNTAX_ERROR(4),
    TYPE_ERROR(5),
    URI_ERROR(6);

    private int errorCode;

    JavascriptErrorCodes(int errorCode)
    {

        this.errorCode = errorCode;
    }

    public int getErrorCode()
    {
        return errorCode;
    }
}