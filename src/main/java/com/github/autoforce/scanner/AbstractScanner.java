package com.github.autoforce.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.ContainerAsyncRequest;
import com.sforce.soap.tooling.sobject.MetadataContainer;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public abstract class AbstractScanner
{
    protected ToolingConnection conn;
    protected ExecutorService executor;
    private boolean workspace;

    public AbstractScanner(LoginResult loginResult) throws ConnectionException
    {
	conn = LoginUtil.createToolingConnection(loginResult);

	executor = Executors.newCachedThreadPool();
    }

    public void setUseWorkspace(boolean workspace)
    {
	this.workspace = workspace;
    }

    protected List<String> copyToContainer(String containerId, SaveResult[] saveResults) throws ConnectionException
    {
	List<String> ids = new ArrayList<String>();
	for (SaveResult saveResult : saveResults)
	    ids.add(saveResult.getId());

	// Create ContainerAysncRequest to deploy the (check only) the Apex Classes and
	// thus obtain the SymbolTable's
	ContainerAsyncRequest ayncRequest = new ContainerAsyncRequest();
	ayncRequest.setMetadataContainerId(containerId);

	// Use the IsCheckOnly parameter on ContainerAsyncRequest to indicate whether an
	// asynchronous request should compile
	// code without making any changes to the organization (true) or compile and
	// save the code (false). (tooling api doc page 6)
	ayncRequest.setIsCheckOnly(true);
	saveResults = conn.create(new ContainerAsyncRequest[] { ayncRequest });

	String containerAsyncRequestId = saveResults[0].getId();
	ayncRequest = (ContainerAsyncRequest) conn.retrieve("State", "ContainerAsyncRequest", new String[] { containerAsyncRequestId })[0];

	while (ayncRequest.getState().equals("Queued"))
	{
	    try
	    {
		Thread.sleep(1 * 1000); // Wait for a second
	    }
	    catch (InterruptedException ex)
	    {
		Thread.currentThread().interrupt();
	    }
	    ayncRequest = (ContainerAsyncRequest) conn.retrieve("State", "ContainerAsyncRequest", new String[] { containerAsyncRequestId })[0];
	}

	return ids;
    }

    protected String createContainer(String name) throws ConnectionException
    {
	MetadataContainer container = new MetadataContainer();
	container.setName(name);

	SaveResult[] saveResults = conn.create(new MetadataContainer[] { container });
	String containerId = saveResults[0].getId();

	return containerId;
    }

    protected void deleteContainer(String name) throws ConnectionException
    {
	// Delete existing MetadataContainer?
	List<MetadataContainer> containers = cast(conn.query("select Id, Name from MetadataContainer where Name = '" + name + "'").getRecords(), MetadataContainer.class);

	List<String> ids = new ArrayList<String>();
	for (MetadataContainer metadataContainer : containers)
	{
	    ids.add(metadataContainer.getId());
	}

	if (containers.size() > 0)
	    conn.delete(ids.toArray(new String[ids.size()]));
    }

    protected static <T> List<T> cast(SObject[] sObjects, Class<T> clazz)
    {
	List<T> list = new ArrayList<T>();

	for (SObject so : sObjects)
	{
	    T t = clazz.cast(so);
	    list.add(t);
	}

	return list;
    }

    public void shutdown()
    {
	executor.shutdown();
    }

    protected void print(List<String> input, Map<String, Set<String>> references)
    {
	System.out.println("+-----found reference classes---------------------for type : " + input);

	for (String key : references.keySet())
	{

	    Set<String> set = references.get(key);

	    System.out.println("+----------------------");
	    System.out.println(key);
	    System.out.println(set);
	}
    }

    protected void getReferenceClasses(List<String> input, String query, String name) throws Exception
    {
	// run in pagination
	conn.getConfig().setCompression(true);

	Map<String, Set<String>> references = new HashMap<String, Set<String>>();

	QueryResult queryResult = conn.query(query);

	boolean done = false;

	if (queryResult.getSize() > 0)
	{
	    System.out.println("total records: " + queryResult.getSize());

	    List<Future<Map<String, Set<String>>>> futureList = new ArrayList<Future<Map<String, Set<String>>>>();

	    int i = 1;

	    while (!done)
	    {
		System.out.println("run .. " + i);

		String containerName = name + i;

		Callable<Map<String, Set<String>>> get = getTask(i, queryResult, containerName, input, workspace);

		Future<Map<String, Set<String>>> future = executor.submit(get);
		futureList.add(future);

		System.out.println("is done .. " + queryResult.isDone());

		if (queryResult.isDone())
		{
		    done = true;
		}
		else
		{
		    queryResult = conn.queryMore(queryResult.getQueryLocator());
		}

		i += 1;
	    }

	    for (Future<Map<String, Set<String>>> future : futureList)
	    {

		Map<String, Set<String>> output = future.get();

		for (String key : output.keySet())
		{

		    Set<String> newSet = output.get(key);

		    if (references.containsKey(key))
		    {
			Set<String> existingSet = references.get(key);

			existingSet.addAll(newSet);
		    }
		    else
		    {
			references.put(key, newSet);
		    }
		}

	    }
	}
	else
	{
	    System.out.println("No records found.");
	}
	System.out.println("\nQuery succesfully executed.");

	print(input, references);
    }

    public abstract Callable<Map<String, Set<String>>> getTask(int id, QueryResult queryResult, String containerId, List<String> input, boolean workspace);

}
