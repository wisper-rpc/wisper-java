package com.widespace.wisper;

import com.widespace.wisper.base.RPCUtilities;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

public class RPCUtilitiesTests
{

    @Before
    public void setUp() throws Exception
    {

    }

    @Test
    public void testConversionToRpcParamsWorks() throws Exception
    {
        Class[] classes = {String.class, Number.class, Boolean.class, HashMap.class};
        List<Class> classList = Arrays.asList(classes);
        WisperParameterType[] wisperParameterTypes = RPCUtilities.convertParameterTypesToRPCParameterType(classList);

        WisperParameterType[] expected = {WisperParameterType.STRING, WisperParameterType.NUMBER, WisperParameterType.BOOLEAN, WisperParameterType.HASHMAP};
        assertTrue(Arrays.equals(expected, wisperParameterTypes));
    }

    @Test
    public void testConversionToRpcParamsThrowsExceptionOnIllegalParams() throws Exception
    {
        Class[] classes = {String.class, Number.class, Integer.class, boolean.class};
        List<Class> classList = Arrays.asList(classes);
        try
        {
            RPCUtilities.convertParameterTypesToRPCParameterType(classList);
            fail("An illegal argument exception was supposed to be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertNotNull(e);
        }
    }
}

