package com.widespace.wisper.annotations;

import com.widespace.wisper.classrepresentation.RPCClassPropertyMode;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;


@RPCClass(name = "wisp.test.annotationTest")
public class RPCAnnotationTest
{
    @RPCProperty(name = "value", mode = RPCClassPropertyMode.READ_WRITE, paramType = RPCMethodParameterType.NUMBER)
    private int testProperty;

    @RPCInstanceMethod(name = "add")
    private int testAddInstance(Number a, Number b)
    {
        return a.intValue() + b.intValue();
    }

    @RPCStaticMethod(name = "add")
    private static int testAddStatic(Number a, Number b)
    {
        return a.intValue() + b.intValue();
    }

    public void setTestProperty(int testProperty)
    {
        this.testProperty = testProperty;
    }
}
