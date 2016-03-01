package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class WisperEventHandlerTest
{
    @Test(expected = WisperException.class)
    public void givenNonEventMessage_throwsException() throws Exception
    {
        Notification nonEventMessage = new Notification();
        nonEventMessage.setMethodName("a.b.c:d");
        new WisperEventHandler(mock(Router.class), mock(WisperClassModel.class), nonEventMessage);
    }

    @Test(expected = WisperException.class)
    public void givenNonNotificationMessage_ShouldThrowException() throws Exception
    {
        Request nonNotif = new Request().withMethodName("a.b.a!");
        new WisperEventHandler(mock(Router.class), mock(WisperClassModel.class), nonNotif);
    }

    @Test
    public void givenStaticEventType_CallsOnClass() throws Exception
    {


    }
}