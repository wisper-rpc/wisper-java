package com.widespace.wisper.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.RUNTIME)
public @interface RPCClass
{
    String name();
}
