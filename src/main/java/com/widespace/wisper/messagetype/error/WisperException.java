package com.widespace.wisper.messagetype.error;

/**
 * Created by ehssanhoorvash on 20/11/15.
 */
public class WisperException extends Exception
{
    private final Error error;
    private final String message;
    private final Exception underlyingException;

    public WisperException(Error error, Exception underlyingException, String message)
    {
        this.error = error;
        this.message = message;
        this.underlyingException = underlyingException;
    }
}
