package com.github.autoforce.scanner;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.Connector;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class LoginUtil
{
    private static String sessionId;
    private static String serverUrl;

    public static ToolingConnection createToolingConnection(LoginResult loginResult) throws ConnectionException
    {
	final ConnectorConfig config = new ConnectorConfig();

	String serverUrl = getServerUrl(loginResult.getServerUrl(), "/T/");
	config.setServiceEndpoint(serverUrl);

	config.setSessionId(loginResult.getSessionId());

	return Connector.newConnection(config);
    }

    public static String getServerUrl(String url, String endPoint)
    {
	String serverUrl = url.replace("/c/", endPoint);

	int index = serverUrl.lastIndexOf("/");
	serverUrl = serverUrl.substring(0, index);

	return serverUrl;
    }

    public static LoginResult loginToSalesforce(String username, String password, String endPoint) throws ConnectionException
    {
	final ConnectorConfig config = new ConnectorConfig();
	config.setAuthEndpoint(endPoint);
	config.setServiceEndpoint(endPoint);
	config.setManualLogin(true);

	EnterpriseConnection ec = new EnterpriseConnection(config);

	LoginResult lr = ec.login(username, password);

	serverUrl = lr.getServerUrl();
	sessionId = lr.getSessionId();

	return lr;
    }

    public static String getSessionId()
    {
	return sessionId;
    }

    public static String getServerUrl()
    {
	return serverUrl;
    }

}