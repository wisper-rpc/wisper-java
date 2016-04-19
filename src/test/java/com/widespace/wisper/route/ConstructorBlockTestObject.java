package com.widespace.wisper.route;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.CallBlock;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.Request;

/**
 * Created by ehssanhoorvash on 19/04/16.
 */
public class ConstructorBlockTestObject implements Wisper
{
    private ClassRouter classRouter;
    private String initialization_id;

    public static WisperClassModel registerRpcClass()
    {
        WisperClassModel classModel = new WisperClassModel(ConstructorBlockTestObject.class);
        classModel.addStaticMethod(new WisperMethod("~", new CallBlock()
        {
            @Override
            public void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, Request message) throws Exception
            {
                if (wisperInstanceModel!=null)
                {
                    ConstructorBlockTestObject obj = (ConstructorBlockTestObject) wisperInstanceModel.getInstance();
                    obj.setInitialization_id("block");

                    // This must be done manually
                    message.getResponseBlock().perform();
                    WisperInstanceRegistry.sharedInstance().addInstance(wisperInstanceModel, router.getRootRoute());
                }
            }
        }));

        return classModel;
    }

    public String getInitializationId()
    {
        return initialization_id;
    }

    public void setInitialization_id(String initialization_id)
    {
        this.initialization_id = initialization_id;
    }

    @Override
    public void setClassRouter(ClassRouter classRouter)
    {
        this.classRouter = classRouter;
    }

    @Override
    public void destruct()
    {

    }
}
