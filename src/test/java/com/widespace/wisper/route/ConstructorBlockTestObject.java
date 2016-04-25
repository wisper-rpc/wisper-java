package com.widespace.wisper.route;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.CallBlock;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.Response;
import com.widespace.wisper.utils.ClassUtils;

import java.util.HashMap;


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
            public void perform(ClassRouter router, WisperInstanceModel wisperInstanceModel, WisperMethod methodModel, AbstractMessage message) throws Exception
            {
                // 1. Remember, default constructor ConstructorBlockTestObject() is always called first. That's because we need the instance for block.
                // In Objective-C they can just do [ClassName alloc] and get an instance without running the initializer, whereas in Java we have to run
                // an actual constructor to get the instance.

                ConstructorBlockTestObject obj = (ConstructorBlockTestObject) wisperInstanceModel.getInstance();

                // 2. Do your initialization, whatever it may be.
                obj.setInitialization_id("block");

                // 3. Prepare the response and Respond back to the request. Do not forget to add the initialized properties in props field. You can do it even manually if
                // you are sure about which properties you wanna send over.

                Response response = ((Request) message).createResponse();
                HashMap<String, Object> idWithProperties = new HashMap<String, Object>();
                idWithProperties.put("id", wisperInstanceModel.getInstanceIdentifier());
                idWithProperties.put("props", ClassUtils.fetchInitializedProperties(wisperInstanceModel, wisperInstanceModel.getWisperClassModel()));
                response.setResult(idWithProperties);

                if (((Request) message).getResponseBlock() != null)
                {
                    ((Request) message).getResponseBlock().perform(response, null);
                }

                // Note: Instance is automatically added to the WisperInstanceRegistry under router.getRootRoute().
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
