package com.widespace.wisper;

import com.widespace.wisper.classrepresentation.CallBlock;
import com.widespace.wisper.classrepresentation.WisperClassInstance;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.classrepresentation.RPCMethodParameterType;
import com.widespace.wisper.controller.RemoteObjectController;
import com.widespace.wisper.messagetype.Request;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class RpcClassMethodTests
{

    private final String SAMPLE_MAP_NAME = "SAMPLE_MAP_NAME";
    private WisperMethod wisperMethod;

    @Before
    public void setUp() throws Exception
    {
        wisperMethod = new WisperMethod(SAMPLE_MAP_NAME, "methodName");
    }

    @Test
    public void testMapNameIsCorrect() throws Exception
    {
        assertThat(SAMPLE_MAP_NAME, is(wisperMethod.getMapName()));
    }


    @Test
    public void testParameterTypesAreCorrect() throws Exception
    {
        wisperMethod = new WisperMethod(SAMPLE_MAP_NAME, "methodName", RPCMethodParameterType.HASHMAP,
                RPCMethodParameterType.STRING, RPCMethodParameterType.NUMBER, RPCMethodParameterType.ARRAY);
        Class[] parameterTypes = wisperMethod.getParameterTypes();
        List<Class> classList = Arrays.asList(parameterTypes);

        assertEquals(classList.size(), 4);
        assertEquals(HashMap.class, classList.get(0));
        assertEquals(String.class, classList.get(1));
        assertEquals(Number.class, classList.get(2));
        assertEquals(Object[].class, classList.get(3));
    }

    @Test
    public void testCallBlockSetsProperly() throws Exception
    {
        CallBlock sampleCallBlock = new CallBlock()
        {
            @Override
            public void perform(RemoteObjectController remoteObjectController, WisperClassInstance classInstance, WisperMethod method, Request request)
            {

            }
        };

        wisperMethod.setCallBlock(sampleCallBlock);
        assertEquals(sampleCallBlock, wisperMethod.getCallBlock());
    }
}
