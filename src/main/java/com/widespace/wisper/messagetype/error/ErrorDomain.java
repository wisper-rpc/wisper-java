package com.widespace.wisper.messagetype.error;

/**
 * Created by Ehssan Hoorvash on 03/06/14.
 */
public enum ErrorDomain
{
    JAVASCRIPT(1),
    NATIVE(2);

    private final int domainCode;

    ErrorDomain(int domainCode)
    {
        this.domainCode = domainCode;
    }

    public int getDomainCode()
    {
        return domainCode;
    }
}
