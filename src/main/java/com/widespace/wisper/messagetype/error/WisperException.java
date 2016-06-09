package com.widespace.wisper.messagetype.error;


public class WisperException extends RuntimeException
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

    public Error getError()
    {
        return error;
    }

    public int getErrorCode()
    {
        return error.getCode();
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    public Exception getUnderlyingException()
    {
        return underlyingException;
    }
}
