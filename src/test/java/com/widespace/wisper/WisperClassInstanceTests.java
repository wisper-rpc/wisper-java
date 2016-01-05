package com.widespace.wisper;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperInstanceModel;
import com.widespace.wisper.controller.RemoteObjectController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WisperClassInstanceTests
{

    public static final String INSTANCE_IDENTIFIER = "myTestId123";
    private WisperInstanceModel wisperInstanceModel;
    private Wisper sampleInstance;

    @Before
    public void setUp() throws Exception
    {
        sampleInstance = new Wisper()
        {
            @Override
            public void setRemoteObjectController(RemoteObjectController remoteObjectController)
            {

            }

            @Override
            public void destruct()
            {

            }
        };
        wisperInstanceModel = new WisperInstanceModel(null, sampleInstance, INSTANCE_IDENTIFIER);
    }

    @Test
    public void testIdentifierIsCorrect() throws Exception
    {
        assertEquals(INSTANCE_IDENTIFIER, wisperInstanceModel.getInstanceIdentifier());
    }


    @Test
    public void testInstanceIsCorrect() throws Exception
    {
        assertEquals(sampleInstance, wisperInstanceModel.getInstance());

    }
}