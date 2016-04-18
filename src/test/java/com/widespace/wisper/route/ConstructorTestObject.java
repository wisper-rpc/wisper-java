package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.*;


public class ConstructorTestObject implements Wisper
{
    private RoutesTestObject constructorParam;
    private ClassRouter classRouter;

    public static WisperClassModel registerRpcClass()
    {
        WisperClassModel classModel = new WisperClassModel(ConstructorTestObject.class);

        WisperMethod customConstructor1 = new WisperMethod("~", "ConstructorTestObject", WisperParameterType.INSTANCE);
        classModel.addStaticMethod(customConstructor1);

        return classModel;
    }

    public ConstructorTestObject(RoutesTestObject instanceParam)
    {
        constructorParam = instanceParam;
    }

    public RoutesTestObject getConstructorParam()
    {
        return constructorParam;
    }

    @Override
    public void setClassRouter(ClassRouter classRouter)
    {
        this.classRouter = classRouter;
    }

    @Override
    public void destruct()
    {
        classRouter = null;
        constructorParam = null;
    }
}
