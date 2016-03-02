package com.widespace.wisper.route;

import com.widespace.wisper.classrepresentation.WisperClassModel;
import com.widespace.wisper.classrepresentation.WisperMethod;
import com.widespace.wisper.messagetype.Event;
import com.widespace.wisper.messagetype.Notification;
import com.widespace.wisper.messagetype.RPCEventBuilder;
import com.widespace.wisper.messagetype.Request;
import com.widespace.wisper.messagetype.error.WisperException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;


public class WisperEventHandlerTest
{

    @Before
    public void setUp() throws Exception
    {
        RoutesTestObject.reset();
    }

    @Test(expected = WisperException.class)
    public void givenNonEventMessage_throwsException() throws Exception
    {
        Notification nonEventMessage = new Notification();
        nonEventMessage.setMethodName("a.b.c:d");
        new WisperEventHandler(mock(Router.class), mock(WisperClassModel.class), nonEventMessage).handle();
    }

    @Test(expected = WisperException.class)
    public void givenNonEventMessage_ShouldThrowException() throws Exception
    {
        Request nonNotif = new Request().withMethodName("a.b.a!");
        new WisperEventHandler(mock(Router.class), mock(WisperClassModel.class), nonNotif).handle();
    }

    @Test
    public void givenStaticEventTypeWithExistingName_publicStaticPropertySetsOnClass() throws Exception
    {
        Event staticEvent = new RPCEventBuilder().withMethodName("a.b.c").withName("testProp").withValue("xxxy").buildStaticEvent();
        WisperClassModel classModel = RoutesTestObject.registerRpcClass();

        assertThat(RoutesTestObject.testProp, is(nullValue()));
        new WisperEventHandler(mock(Router.class), classModel, staticEvent).handle();
        assertThat(RoutesTestObject.testProp, is("xxxy"));
    }

    @Test
    public void givenStaticEventTypeWithNonExistingName_staicHandlerCalledOnClass() throws Exception
    {
        Event staticEvent = new RPCEventBuilder().withMethodName("a.b.c").withName("nonExistingName").withValue("xxxy").buildStaticEvent();
        WisperClassModel classModel = RoutesTestObject.registerRpcClass();

        assertThat(RoutesTestObject.testProp, is(nullValue()));
        new WisperEventHandler(mock(Router.class), classModel, staticEvent).handle();
        assertThat(RoutesTestObject.testProp, is(nullValue()));
        assertThat(RoutesTestObject.isStaticEventReceived(), is(true));
    }

    @Test
    public void givenStaticEventTypeWithNonExistingNameAndHandler_shouldRunSilently() throws Exception
    {
        Event staticEvent = new RPCEventBuilder().withMethodName("a.b.c").withName("nonExistingName").withValue("xxxy").buildStaticEvent();
        WisperClassModel classModel = mock(WisperClassModel.class);

        new WisperEventHandler(mock(Router.class), classModel, staticEvent).handle();
    }

    @Test(expected = WisperException.class)
    public void givenInstanceEvent_shouldThrowExceptionOnNonExistingInstance() throws Exception
    {
        Event instanceEvent = new RPCEventBuilder().withMethodName("a.b.c").withName("nonExistingName").withValue("xxxy").withInstanceIdentifier("nonExistent-1123").buildInstanceEvent();
        new WisperEventHandler(mock(Router.class), RoutesTestObject.registerRpcClass(), instanceEvent).handle();
    }
}