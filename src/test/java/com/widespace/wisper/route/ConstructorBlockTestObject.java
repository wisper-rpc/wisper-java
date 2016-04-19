package com.widespace.wisper.route;

import com.widespace.wisper.base.Constants;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.CallBlock;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.utils.ClassUtils;

import java.util.HashMap;

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

                    //Response back to the request
                    Response response = message.createResponse();
                    HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
                    idWithProperties.put("id", wisperInstanceModel.getInstanceIdentifier());
                    idWithProperties.put("props", ClassUtils.fetchInitializedProperties(wisperInstanceModel, wisperInstanceModel.getWisperClassModel()));
                    response.setResult(idWithProperties);

                    if (message.getResponseBlock() != null)
                    {
                        message.getResponseBlock().perform(response, null);
                    }
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
