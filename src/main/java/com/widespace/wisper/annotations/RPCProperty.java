package com.widespace.wisper.annotations;

import com.widespace.wisper.classrepresentation.WisperPropertyAccess;
import com.widespace.wisper.classrepresentation.WisperParameterType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Ehssan Hoorvash on 16/01/15.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface RPCProperty
{
    String name();

    WisperPropertyAccess mode();

    String setter() default "[Unassigned]";

    WisperParameterType paramType();
}
