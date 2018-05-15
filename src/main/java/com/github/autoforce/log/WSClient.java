package com.github.autoforce.log;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class WSClient extends WebSocketClient
{
    private static WSClient client;

    public static WSClient getWSClient()
    {
	if (client == null)
	{
	    try
	    {
		client = new WSClient(new URI("ws://localhost:8080/tool/log"));
		client.connectBlocking();
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}

	return client;
    }

    public WSClient(URI serverUri, Draft draft)
    {
	super(serverUri, draft);
    }

    public WSClient(URI serverURI)
    {
	super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata)
    {
	System.out.println("new connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
	System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(String message)
    {
	// System.out.println("received message: " + message);
    }

    @Override
    public void onError(Exception ex)
    {
	System.err.println("an error occured:" + ex);
    }

}