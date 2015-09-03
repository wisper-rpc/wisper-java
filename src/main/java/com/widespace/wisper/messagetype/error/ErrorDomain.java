package com.widespace.wisper.messagetype.error;

/**
 * Created by Ehssan Hoorvash on 03/06/14.
 */
public enum ErrorDomain
{
    ANDROID(20),
    JAVASCRIPT(0),
    RPC(1),
    REMOTE_OBJECT(2);

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
