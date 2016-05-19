package com.widespace.wisper.messagetype;

import com.widespace.wisper.route.MessageParser;
import com.widespace.wisper.route.WisperCallType;

import java.util.ArrayList;


/**
 * Model object representing an event to be sent or received.
 * <p/>
 * Created by Ehssan Hoorvash on 17/06/14.
 */
public class Event extends Notification
{
    private final String methodName;
    private String instanceIdentifier;
    private String name;
    private Object value;


    public Event(String methodName, Object... params)
    {
        super(methodName, params);
        this.methodName = methodName.replace(":!", "").replace("!", "");

        if (params.length == 2)
        {
            name = (String)params[ 0 ];
            value = params[ 1 ];
        } else if (params.length == 3) {
            instanceIdentifier = (String)params[ 0 ];
            name = (String)params[ 1 ];
            value = params[ 2 ];
        }
    }

    public Event(Notification notification)
    {
        super(notification.getMethodName(), notification.getParams());
        this.methodName = notification.getMethodName().replace(":!", "").replace("!", "");
        WisperCallType callType = MessageParser.getCallType(notification);
        Object[] notificationParams = notification.getParams();
        switch (callType)
        {
            case STATIC_EVENT:
            {
                this.instanceIdentifier = null;
                if (notificationParams.length > 1)
                {
                    this.name = (String) notificationParams[0];
                    this.value = notificationParams[1];
                }
            }
            break;

            case INSTANCE_EVENT:
            {
                this.instanceIdentifier = (String) notificationParams[0];
                if (notificationParams.length > 2)
                {
                    this.name = (String) notificationParams[1];
                    this.value = notificationParams[2];
                }
            }

        }


    }


    public String getInstanceIdentifier()
    {
        return instanceIdentifier;
    }

    public void setInstanceIdentifier(String instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    @Override
    public Object[] getParams()
    {
        ArrayList<Object> params = new ArrayList<Object>();
        if (instanceIdentifier != null)
        {
            params.add(instanceIdentifier);
        }

        params.add(name == null ? "" : name);
        params.add(value == null ? "" : value);

        return params.toArray(new Object[params.size()]);
    }


}
