package com.widespace.wisper.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Ehssan Hoorvash on 16/01/15.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface RPCInstanceMethod
{
    String name();
}
