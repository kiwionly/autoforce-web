package test;

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
import java.util.concurrent.ExecutionException;
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
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class ToolingAPI
{

    public static void main(String[] args) throws Exception
    {
	// System.out.println(new Date(1416482426000));

	LoginResult login = login();
	ToolingConnection tooling = LoginUtil.createToolingConnection(login);

	long start = System.currentTimeMillis();

	execute(tooling);

	long end = System.currentTimeMillis();

	System.out.println(end - start);

    }

    private static void execute(ToolingConnection tooling) throws ConnectionException, IOException, InterruptedException, ExecutionException
    {
	ExecutorService pool = Executors.newFixedThreadPool(10);

	List<Future<String>> list = new ArrayList<Future<String>>();

	// ExecuteAnonymousResult r = tooling.executeAnonymous(" system.debug('hello
	// world! wahahaha'); ");
	//
	// if (r.isSuccess()) {
	// System.out.println("Code executed successfully");
	// }
	// else {
	// System.out.println("Exception message: " + r.getCompileProblem());
	// System.out.println("Exception stack trace: " + r.getExceptionStackTrace());
	//
	// return;
	// }

	String id = "005Q0000001Bsa7IAC";

	createTraceFlag(tooling, id);

	List<String> ids = queryTraceFlag(tooling, id);

	// DeleteResult[] y = tooling.delete(ids.toArray(new String[ids.size()]));

	boolean x = true;

	if (x)
	    return;

	QueryResult result = tooling.query("select id, Request from ApexLog where LogUserId = '00530000007Va1x' order by StartTime desc limit 5");

	// String id = result.getRecords()[0].getId();

	for (SObject sobject : result.getRecords())
	{
	    ApexLog log = (ApexLog) sobject;

	    Future<String> future = pool.submit(new FetchLogTask(log.getId(), log.getRequest()));

	    list.add(future);
	}

	for (Future<String> future : list)
	{
	    String output = future.get();

	    System.out.println(output);
	}

	pool.shutdown();
    }

    private static List<String> queryTraceFlag(ToolingConnection tooling, String id) throws ConnectionException
    {
	List<String> ids = new ArrayList<String>();

	QueryResult trace = tooling.query(
		"select id, ApexCode, ApexProfiling, Callout, Database, ExpirationDate, Validation, Visualforce, System, TracedEntityId from TraceFlag " + " where TracedEntityId  = '" + id + "' ");

	for (SObject sobject : trace.getRecords())
	{
	    TraceFlag flag = (TraceFlag) sobject;

	    System.out.println(flag.getApexCode());
	    System.out.println(flag.getApexProfiling());
	    System.out.println(flag.getCallout());
	    System.out.println(flag.getDatabase());
	    System.out.println(flag.getVisualforce());
	    System.out.println(flag.getValidation());
	    System.out.println(flag.getSystem());
	    System.out.println(flag.getTracedEntityId());
	    System.out.println(flag.getExpirationDate());
	    System.out.println("-------");

	    ids.add(flag.getId());
	}

	return ids;
    }

    private static void createTraceFlag(ToolingConnection tooling, String userId) throws ConnectionException
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
	cal.add(Calendar.HOUR_OF_DAY, 5); // adds one hour

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

			builder.append(request + ":");
			builder.append(out.trim());
		    }
		}

		buf.close();
	    }
	});

	return builder.toString();
    }

    public interface ResponseCallback
    {
	public void process(String response) throws IOException;
    }

    public static void restGET(String sessionID, String id, ResponseCallback callback) throws IOException
    {
	CloseableHttpClient client = HttpClients.createDefault();

	HttpGet get = new HttpGet("https://domain--test.cs30.my.salesforce.com/services/data/v32.0/tooling/sobjects/ApexLog/" + id + "/Body/");
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
	String USERNAME = "";
	String PASSWORD = "";
	String URL = "";
	LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return loginResult;
    }
}
