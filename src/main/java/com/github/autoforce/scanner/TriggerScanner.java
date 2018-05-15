
package com.github.autoforce.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.*;
import com.sforce.ws.ConnectionException;

public class TriggerScanner<T> extends AbstractScanner
{
    public TriggerScanner(LoginResult loginResult) throws ConnectionException
    {
	super(loginResult);
    }

    public void getReferenceClass(List<String> input) throws Exception
    {
	getReferenceClasses(input, "select Id, Name, Body from ApexTrigger where NamespacePrefix = null order by Name asc ", "referenceTrigger");
    }

    @Override
    public Callable<Map<String, Set<String>>> getTask(int id, QueryResult queryResult, String containerId, List<String> input, boolean workspace)
    {
	return new GetTask(id, queryResult, containerId, input, workspace);
    }

    private class GetTask implements Callable<Map<String, Set<String>>>
    {
	private int id;
	private QueryResult queryResult;
	private String containerName;
	private List<String> input;
	private boolean workspace;

	public GetTask(int id, QueryResult queryResult, String containerName, List<String> input, boolean workspace)
	{
	    this.id = id;
	    this.queryResult = queryResult;
	    this.containerName = containerName;
	    this.input = input;
	    this.workspace = workspace;
	}

	@Override
	public Map<String, Set<String>> call() throws Exception
	{
	    List<String> ids = new ArrayList<String>();
	    Map<String, Set<String>> output = null;

	    if (workspace)
	    {
		deleteContainer(containerName);
		String containerId = createContainer(containerName);

		SObject[] records = queryResult.getRecords();

		List<ApexTrigger> list = cast(records, ApexTrigger.class);

		List<ApexTriggerMember> memberList = getApexTriggerMember(list, containerId);
		System.out.println(id + " - class size =  " + memberList.size());
		SaveResult[] saveResults = conn.create(memberList.toArray(new ApexTriggerMember[memberList.size()]));
		System.out.println(id + " - create class size =  " + saveResults.length);
		ids = copyToContainer(containerId, saveResults);
		System.out.println(id + " - ids size from container =  " + ids.size());

		output = getReferenceClasses(input, ids);
	    }
	    else
	    {
		SObject[] list = queryResult.getRecords();

		for (SObject sObject : list)
		{

		    ApexTrigger apex = (ApexTrigger) sObject;

		    ids.add(apex.getName());
		}

		output = getReferenceClassesNoWorkSpace(input, ids);
	    }

	    // debug use, print out
	    for (String key : output.keySet())
	    {

		Set<String> set = output.get(key);

		System.out.println(key + " = " + set);
	    }

	    return output;
	}
    }

    private List<ApexTriggerMember> getApexTriggerMember(List<ApexTrigger> list, String containerId) throws ConnectionException
    {
	List<ApexTriggerMember> result = new ArrayList<ApexTriggerMember>();

	for (ApexTrigger apexTrigger : list)
	{
	    ApexTriggerMember trigger = new ApexTriggerMember();

	    trigger.setBody(apexTrigger.getBody());
	    trigger.setContentEntityId(apexTrigger.getId());
	    trigger.setMetadataContainerId(containerId);
	    trigger.setFullName(apexTrigger.getName());

	    // System.out.println(trigger.getName());

	    result.add(trigger);
	}

	return result;
    }

    protected Map<String, Set<String>> getReferenceClasses(List<String> inputList, List<String> ids) throws ConnectionException
    {
	List<ApexTriggerMember> apexClassMembersWithSymbols = cast(conn.retrieve("FullName, ContentEntityId, SymbolTable", "ApexTriggerMember", ids.toArray(new String[ids.size()])),
		ApexTriggerMember.class);

	return getReference(inputList, apexClassMembersWithSymbols);
    }

    protected Map<String, Set<String>> getReferenceClassesNoWorkSpace(List<String> inputList, List<String> ids) throws ConnectionException
    {
	StringBuilder buf = new StringBuilder();

	for (String name : ids)
	{

	    buf.append("'" + name + "'");
	    buf.append(",");
	}

	String query = buf.toString();

	String params = query.substring(0, query.length() - 1);

	List<ApexTriggerMember> apexClassMembersWithSymbols = cast(conn.query("select FullName, ContentEntityId, SymbolTable from ApexTriggerMember where FullName in (" + params + ")").getRecords(),
		ApexTriggerMember.class);

	return getReference(inputList, apexClassMembersWithSymbols);
    }

    protected Map<String, Set<String>> getReference(List<String> inputList, List<ApexTriggerMember> apexClassMembersWithSymbols) throws ConnectionException
    {

	Map<String, Set<String>> references = new HashMap<String, Set<String>>();

	for (String input : inputList)
	{
	    input = input.trim();

	    Set<String> referencesClasses = new HashSet<>();

	    String methodName = null;

	    if (input.contains("."))
	    {
		int index = input.indexOf('.');
		methodName = input.substring(index + 1);

		input = input.substring(0, index);
	    }

	    for (ApexTriggerMember trigger : apexClassMembersWithSymbols)
	    {
		// no idea why will null, to skip expcetion
		if (trigger == null)
		    continue;

		// System.out.println("+---------------------------------");
		System.out.println("name = " + trigger.getFullName());

		SymbolTable symbolTable = trigger.getSymbolTable();

		if (symbolTable == null) // No symbol table, then class likely is invalid
		    continue;

		ExternalReference[] x = symbolTable.getExternalReferences();

		Map<String, Set<String>> map = new HashMap<>();

		for (ExternalReference er : x)
		{
		    String key = null;

		    if (er.getNamespace() == null) // name space not null mean this is inner class
			key = er.getName();

		    else
		    {
			if (!er.getNamespace().equalsIgnoreCase("system")) // skip the system class.
			    key = er.getName();
		    }

		    Set<String> methodList = new HashSet<String>();

		    ExternalMethod[] ms = er.getMethods();

		    for (ExternalMethod externalMethod : ms)
		    {
			methodList.add(externalMethod.getName());
		    }

		    if (key != null) // not return system class, causing key = null
			map.put(key, methodList);

		}

		// System.out.println(map);

		for (String key : map.keySet())
		{

		    Set<String> methodList = map.get(key);

		    if (key.equalsIgnoreCase(input))
		    {
			// System.out.println("found reference from this class = " +
			// apexClassMember.getFullName() );

			if (methodName == null)
			    referencesClasses.add(trigger.getFullName());
			else
			    if (methodList.contains(methodName))
			    {
				referencesClasses.add(trigger.getFullName());
			    }
		    }
		}
	    }

	    references.put(input, referencesClasses);
	}

	return references;
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception
    {
	LoginResult loginResult = LoginUtil.loginToSalesforce("gheewooi.ong@domain.com.dev", "", "https://test.salesforce.com/services/Soap/c/32");
	TriggerScanner<?> scanner = new TriggerScanner(loginResult);

	List<String> queryClasses = new ArrayList<String>();
	queryClasses.add("Global_Log_Logger");
	queryClasses.add("Global_Log_RealTimeLogger ");

	scanner.getReferenceClass(queryClasses);

	scanner.shutdown();
    }

}
