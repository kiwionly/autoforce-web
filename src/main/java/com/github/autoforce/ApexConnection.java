package com.github.autoforce;

import com.sforce.soap.apex.Connector;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class ApexConnection
{
    private static SoapConnection conn;

    public ApexConnection(ConnectorConfig config) throws ConnectionException
    {
	conn = Connector.newConnection(config);
    }

    public SoapConnection getConnection()
    {
	return conn;
    }

}
