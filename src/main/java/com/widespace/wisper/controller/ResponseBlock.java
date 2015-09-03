package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.RPCResponse;

/**
 * An interface representing a block in objective C, The block is supposed to be used by RPCResponse message types and
 * upon responding the block may be executed.
 *
 * @see com.widespace.wisper.messagetype.RPCResponse
 * <p/>
 * Created by Ehssan Hoorvash on 22/05/14.
 */

public interface ResponseBlock
{
    /**
     * The piece of code to be executed by teh response.
     *
     * @param response The relevant RPCResponse object
     */
    void perform(RPCResponse response);
}
