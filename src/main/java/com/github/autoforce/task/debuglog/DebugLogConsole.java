package com.github.autoforce.task.debuglog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class DebugLogConsole
{
    public static void main(String[] args) throws Exception
    {
	DebugLogConsole console = new DebugLogConsole();
	console.getDebugLog();
    }

    public void getDebugLog() throws Exception
    {
	LoginResult login = login();
	ToolingConnection tooling = LoginUtil.createToolingConnection(login);

	EnterpriseConnection enterprise = LoginUtil.createConnection(login);

	String id = enterprise.getUserInfo().getUserId();

	long start = System.currentTimeMillis();

	String log = getDebugLog(tooling, id, 5);
	System.out.println(log);

	long end = System.currentTimeMillis();

	System.out.println(end - start);

    }

    public String getDebugLog(ToolingConnection tooling, String userId, int limit) throws Exception
    {
	StringBuilder buf = new StringBuilder();

	ExecutorService pool = Executors.newCachedThreadPool();

	QueryResult result = tooling.query("select id, Request from ApexLog where LogUserId = '" + userId + "' order by StartTime desc limit " + limit);

	List<Future<String>> debugList = new ArrayList<Future<String>>();

	for (SObject sobject : result.getRecords())
	{
	    ApexLog log = (ApexLog) sobject;

	    Future<String> future = pool.submit(new FetchLogTask(log.getId(), log.getRequest()));

	    debugList.add(future);
	}

	for (Future<String> future : debugList)
	{
	    String output = future.get();

	    buf.append(output);
	    buf.append("\n");
	}

	pool.shutdown();

	return buf.toString();
    }

    public static class FetchLogTask implements Callable<String>
    {
	private String id;
	private String request;

	public FetchLogTask(String id, String request)
	{
	    this.id = id;
	    this.request = request;
	}

	@Override
	public String call() throws Exception
	{
	    return fetchLog(id, request);
	}
    }

    public static String fetchLog(String id, final String request) throws IOException
    {
	final StringBuilder builder = new StringBuilder();

	restGET(LoginUtil.getSessionId(), id, new ResponseCallback() {

	    @Override
	    public void process(String response) throws IOException
	    {
		BufferedReader buf = new BufferedReader(new StringReader(response));

		String line = null;

		while ((line = buf.readLine()) != null)
		{
		    if (line.contains("USER_DEBUG"))
		    {
			int index = line.lastIndexOf("|DEBUG|");

			if (index == -1)
			    continue;

			String out = line.substring(index + "|DEBUG|".length());

			builder.append(request + " : ");
			builder.append(out.trim());
		    }
		}

		buf.close();
	    }
	});

	return builder.toString();
    }

    public void createTraceFlag(ToolingConnection tooling, String userId) throws ConnectionException
    {
	// create a new TraceFlag object
	TraceFlag traceFlag = new TraceFlag();
	traceFlag.setApexCode("DEBUG");
	traceFlag.setApexProfiling("INFO");
	traceFlag.setCallout("INFO");
	traceFlag.setDatabase("INFO");
	traceFlag.setSystem("DEBUG");
	traceFlag.setValidation("INFO");
	traceFlag.setVisualforce("INFO");
	traceFlag.setWorkflow("INFO");

	Calendar cal = Calendar.getInstance(); // creates calendar
	cal.setTime(new Date()); // sets calendar time/date
	cal.add(Calendar.HOUR_OF_DAY, 4); // adds one hour

	// set an expiration date
	traceFlag.setExpirationDate(cal);

	// set the ID of the user to monitor
	// traceFlag.setScopeId(null);
	traceFlag.setTracedEntityId(userId);

	// call the create method
	TraceFlag[] traceFlags = { traceFlag };

	SaveResult[] traceResults = tooling.create(traceFlags);

	for (int i = 0; i < traceResults.length; i++)
	{
	    if (traceResults[i].isSuccess())
	    {
		System.out.println("Successfully created trace flag: " + traceResults[i].getId());
	    }
	    else
	    {
		System.out.println("Error: could not create trace flag ");
		System.out.println(" The error reported was: " + traceResults[i].getErrors()[0].getMessage());
	    }
	}
    }

    public interface ResponseCallback
    {
	public void process(String response) throws IOException;
    }

    public static void restGET(String sessionID, String id, ResponseCallback callback) throws IOException
    {
	CloseableHttpClient client = HttpClients.createDefault();

	String url = LoginUtil.getServerUrl();

	int index = url.indexOf("/", 10);
	url = url.substring(0, index);

	HttpGet get = new HttpGet(url + "/services/data/v32.0/tooling/sobjects/ApexLog/" + id + "/Body/");
	get.addHeader(new BasicHeader("Authorization", "Bearer " + sessionID));

	long start = System.currentTimeMillis();

	CloseableHttpResponse response = client.execute(get);

	long stop = System.currentTimeMillis();

	// System.out.println("rest " + (stop - start));

	HttpEntity en = response.getEntity();

	InputStream in = en.getContent();

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	IOUtils.copy(in, out);

	callback.process(out.toString());

	client.close();
    }

    public static LoginResult login() throws ConnectionException
    {
	String USERNAME = "gheewooi.ong@domain.com.test";
	String PASSWORD = "";
	String URL = "https://test.salesforce.com/services/Soap/c/32.0";
	LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return loginResult;
    }
}
