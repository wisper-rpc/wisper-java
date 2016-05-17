package com.widespace.wisper.route;

import com.widespace.wisper.messagetype.AbstractMessage;
import com.widespace.wisper.messagetype.CallMessage;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.Error;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class FunctionRouterTest
{
    @Test
    public void givenFunctionRouter_canRouteMessages() throws Exception
    {
        final Request someMessage = mock(Request.class);

        Router router = new Router();
        router.exposeRoute("a.b.c", new FunctionRouter()
        {
            @Override
            public void routeMessage(CallMessage message, String path) throws WisperException
            {
                assertThat(message, is(notNullValue()));
                assertThat(message, is(instanceOf(Request.class)));
                assertThat((Request) message, is(someMessage));
            }
        });

        router.routeMessage(someMessage, "a.b.c");
    }

    @Test
    public void givenFunctionRouter_canThrowExceptionsBackToParentRouter() throws Exception
    {
        final Request someMessage = mock(Request.class);
        final WisperException sample_exception = new WisperException(Error.UNEXPECTED_TYPE_ERROR, null, "random err");


        Router router = new Router();
        router.exposeRoute("a.b.c", new FunctionRouter()
        {
            @Override
            public void routeMessage(CallMessage message, String path) throws WisperException
            {
                throw sample_exception;
            }
        });

        try
        {
            router.routeMessage(someMessage, "a.b.c");
            fail(); // should never end up here

        } catch (WisperException e)
        {
            assertThat(e, is(notNullValue()));
            assertThat(e.getError(), is(Error.UNEXPECTED_TYPE_ERROR));
            assertThat(e.getMessage(), is("random err"));
            assertThat(e.getUnderlyingException(), is(nullValue()));
        }

    }
}
