package com.widespace.wisper.base;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a message queue to act as a buffer while messages are handled by the JSExecutor.
 * <p/>
 * Created by Ehssan Hoorvash on 12/12/14.
 */
public class MessageQueue<T>
{
    List<T> messages;

    public MessageQueue()
    {
        this.messages = new ArrayList<T>();
    }

    public void push(T message)
    {
        messages.add(message);
    }

    public T pop()
    {
        T s = messages.get(0);
        messages.remove(0);
        return s;
    }

    public boolean hasMessage()
    {
        return !messages.isEmpty();
    }

    public void clear()
    {
        messages.clear();
    }
}
