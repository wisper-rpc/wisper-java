package com.widespace.wisper;

import com.widespace.wisper.utils.RPCUtilities;
import com.widespace.wisper.classrepresentation.WisperParameterType;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RPCUtilitiesTests
{
    @Test
    public void testConversionToRpcParamsWorks() throws Exception
    {
        Class[] classes = {String.class, Number.class, Boolean.class, HashMap.class};
        List<Class> classList = Arrays.asList(classes);
        WisperParameterType[] wisperParameterTypes = RPCUtilities.convertParameterTypesToRPCParameterType(classList);

        WisperParameterType[] expected = {WisperParameterType.STRING, WisperParameterType.NUMBER, WisperParameterType.BOOLEAN, WisperParameterType.HASHMAP};
        assertThat(Arrays.equals(expected, wisperParameterTypes), is(true));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConversionToRpcParamsThrowsExceptionOnIllegalParams() throws Exception
    {
        Class[] classes = {String.class, Number.class, Integer.class, boolean.class};
        List<Class> classList = Arrays.asList(classes);
        RPCUtilities.convertParameterTypesToRPCParameterType(classList);
    }
}

