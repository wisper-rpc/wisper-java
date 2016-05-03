package com.widespace.wisper.base;

import com.widespace.wisper.messagetype.AbstractMessage;

/**
 * A channel through which to send AbstractMessages.
 */
public interface Channel
{
    void sendMessage(AbstractMessage message);
}
