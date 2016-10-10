package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.error.Error;

import org.jetbrains.annotations.Nullable;

/**
 * Created by patrik on 2016-10-05.
 */

public interface AsyncReturn  {

    /**
     * method called when an operation or method is completed.
     *
     * @param result result of the operation, if any, otherwise null.
     * @param error  Descriptive error of what went wrong.
     */
    void perform(@Nullable Object result, @Nullable Error error);
}
