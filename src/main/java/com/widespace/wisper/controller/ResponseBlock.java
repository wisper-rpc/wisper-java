package com.widespace.wisper.controller;

import com.widespace.wisper.messagetype.Response;

/**
 * An interface representing a block in objective C, The block is supposed to be used by Response message types and
 * upon responding the block may be executed.
 *
 * @see Response
 * <p/>
 * Created by Ehssan Hoorvash on 22/05/14.
 */

public interface ResponseBlock
{
    /**
     * The piece of code to be executed by teh response.
     *
     * @param response The relevant Response object
     */
    void perform(Response response);
}
