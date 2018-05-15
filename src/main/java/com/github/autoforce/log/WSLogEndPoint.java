package com.github.autoforce.log;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/log")
public class WSLogEndPoint
{
    @OnMessage
    public void onMessage(Session session, String message)
    {
	try
	{

	    for (Session sess : session.getOpenSessions())
	    {
		if (sess.isOpen())
		{
		    if (message != null)
			sess.getBasicRemote().sendText(message);
		}

	    }

	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
    }
}
