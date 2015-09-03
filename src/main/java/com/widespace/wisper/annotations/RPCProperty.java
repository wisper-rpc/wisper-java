package com.widespace.wisper.annotations;

import com.widespace.wisper.classrepresentation.RPCClassPropertyMode;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Ehssan Hoorvash on 16/01/15.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface RPCProperty
{
    String name();

    RPCClassPropertyMode mode();

    String setter() default "[Unassigned]";

    RPCMethodParameterType paramType();
}
