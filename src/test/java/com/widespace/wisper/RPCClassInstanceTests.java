package com.widespace.wisper;

import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.RPCClassInstance;
import com.widespace.wisper.controller.RPCRemoteObjectController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RPCClassInstanceTests
{

    public static final String INSTANCE_IDENTIFIER = "myTestId123";
    private RPCClassInstance rpcClassInstance;
    private Wisper sampleInstance;

    @Before
    public void setUp() throws Exception
    {
        sampleInstance = new Wisper()
        {
            @Override
            public void setRemoteObjectController(RPCRemoteObjectController remoteObjectController)
            {

            }

            @Override
            public void destruct()
            {

            }
        };
        rpcClassInstance = new RPCClassInstance(null, sampleInstance, INSTANCE_IDENTIFIER);
    }

    @Test
    public void testIdentifierIsCorrect() throws Exception
    {
        assertEquals(INSTANCE_IDENTIFIER, rpcClassInstance.getInstanceIdentifier());
    }


    @Test
    public void testInstanceIsCorrect() throws Exception
    {
        assertEquals(sampleInstance, rpcClassInstance.getInstance());

    }
}