package com.widespace.wisper.base;

import com.widespace.wisper.messagetype.error.RPCErrorMessage;
import org.jetbrains.annotations.Nullable;

/**
 * A general completion block
 */
public interface CompletionBlock
{
    /**
     * method called when an operation or method is completed.
     *
     * @param result result of the operation, if any, otherwise null.
     * @param error  exception, throwable or error resulting from the operation, if any, otherwise null.
     */
    void perform(@Nullable Object result, @Nullable RPCErrorMessage error);
}
