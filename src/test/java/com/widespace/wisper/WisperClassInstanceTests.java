package com.widespace.wisper;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassInstance;
import com.widespace.wisper.controller.RemoteObjectController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WisperClassInstanceTests
{

    public static final String INSTANCE_IDENTIFIER = "myTestId123";
    private WisperClassInstance wisperClassInstance;
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
        wisperClassInstance = new WisperClassInstance(null, sampleInstance, INSTANCE_IDENTIFIER);
    }

    @Test
    public void testIdentifierIsCorrect() throws Exception
    {
        assertEquals(INSTANCE_IDENTIFIER, wisperClassInstance.getInstanceIdentifier());
    }


    @Test
    public void testInstanceIsCorrect() throws Exception
    {
        assertEquals(sampleInstance, wisperClassInstance.getInstance());

    }
}