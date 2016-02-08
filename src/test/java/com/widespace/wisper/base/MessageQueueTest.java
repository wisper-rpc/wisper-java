package com.widespace.wisper.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class MessageQueueTest
{

    private MessageQueue<String> messageQueue;

    @Before
    public void setUp() throws Exception
    {
        messageQueue = new MessageQueue<String>();
    }

    @After
    public void tearDown() throws Exception
    {
        messageQueue.clear();
        messageQueue = null;
    }

    @Test
    public void canPushMessages() throws Exception
    {
        String exampleString = "this is a test";
        messageQueue.push(exampleString);
        assertThat(messageQueue.hasMessage(), is(true));
    }

    @Test
    public void clearFlushesTheQueue() throws Exception
    {
        String exampleString = "this is a test";
        messageQueue.push(exampleString);
        messageQueue.clear();
        assertThat(messageQueue.hasMessage(), is(false));
    }

    @Test
    public void popReturnsObjectOfTheSameType() throws Exception
    {
        String exampleString = "this is a test";
        messageQueue.push(exampleString);
        Object popped = messageQueue.pop();
        assertThat(popped, instanceOf(String.class));
    }

    @Test
    public void popReturnsObjectOfCorrectValue() throws Exception
    {
        String exampleString = "this is a test";
        messageQueue.push(exampleString);
        Object popped = messageQueue.pop();
        assertThat( (String) popped, is(exampleString));
    }

    @Test
    public void popReturnsObjectsAsFIFO() throws Exception
    {
        String exampleString = "this is a test";
        String exampleString2 = "this is also a test";
        String exampleString3 = "this is also a test like the others";

        messageQueue.push(exampleString);
        messageQueue.push(exampleString2);
        messageQueue.push(exampleString3);

        assertThat(messageQueue.pop(), is(exampleString));
        assertThat(messageQueue.pop(), is(exampleString2));
        assertThat(messageQueue.pop(), is(exampleString3));
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void poppingEmptyQueueReturnsException() throws Exception
    {
        messageQueue.clear();
        Object popped = messageQueue.pop();
        Object whatever = "";
        assertThat(popped, is(whatever));
    }
}