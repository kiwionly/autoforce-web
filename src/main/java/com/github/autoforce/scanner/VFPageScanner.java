
package com.github.autoforce.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.*;
import com.sforce.ws.ConnectionException;

public class VFPageScanner<T> extends AbstractScanner
{
    public VFPageScanner(LoginResult loginResult) throws ConnectionException
    {
	super(loginResult);
    }

    public void getReferencePage(List<String> input) throws Exception
    {
	getReferenceClasses(input, "select Id, Name, Markup from ApexPage order by Name asc ", "referencePage");
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

		List<ApexPage> list = cast(records, ApexPage.class);

		List<ApexPageMember> memberList = getApexClassMember(list, containerId);
		System.out.println(id + " - class size =  " + memberList.size());
		SaveResult[] saveResults = conn.create(memberList.toArray(new ApexPageMember[memberList.size()]));
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

		    ApexPage apex = (ApexPage) sObject;

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

    private List<ApexPageMember> getApexClassMember(List<ApexPage> list, String containerId) throws ConnectionException
    {
	List<ApexPageMember> result = new ArrayList<ApexPageMember>();

	for (ApexPage apexPage : list)
	{
	    ApexPageMember page = new ApexPageMember();

	    page.setBody(apexPage.getMarkup());
	    page.setContentEntityId(apexPage.getId());
	    page.setMetadataContainerId(containerId);
	    page.setFullName(apexPage.getName());

	    // System.out.println(apexClass.getName());

	    result.add(page);
	}

	return result;
    }

    protected Map<String, Set<String>> getReferenceClasses(List<String> inputList, List<String> ids) throws ConnectionException
    {
	List<ApexPageMember> apexPages = cast(conn.retrieve("FullName, ContentEntityId, Body", "ApexPageMember", ids.toArray(new String[ids.size()])), ApexPageMember.class);

	return getReferencePage(inputList, apexPages);
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

	List<ApexPageMember> apexPages = cast(conn.query("select FullName, ContentEntityId, Body from ApexPageMember where FullName in (" + params + ")").getRecords(), ApexPageMember.class);

	return getReferencePage(inputList, apexPages);
    }

    private Map<String, Set<String>> getReferencePage(List<String> inputList, List<ApexPageMember> apexPages) throws ConnectionException
    {
	Map<String, Set<String>> references = new HashMap<String, Set<String>>();

	for (String input : inputList)
	{
	    input = input.trim();

	    Set<String> referencesClasses = new HashSet<>();

	    for (ApexPageMember apexPageMember : apexPages)
	    {
		String name = apexPageMember.getFullName();
		String body = apexPageMember.getBody();

		Document doc = Jsoup.parse(body);

		Elements elements = doc.getElementsByTag("apex:page");

		for (Element ele : elements)
		{
		    // standard controller
		    String standard = ele.attr("standardController");

		    if (standard.equals(input))
			referencesClasses.add(standard);

		    // controller
		    String controller = ele.attr("controller");

		    if (controller.equals(input))
			referencesClasses.add(controller);

		    // extension
		    String extensions = ele.attr("extension");

		    for (String ext : extensions.split(","))
		    {
			if (ext.equals(input))
			    referencesClasses.add(ext);
		    }

		    System.out.println(name + " controller: " + controller + " extension : " + extensions);
		}
	    }

	    references.put(input, referencesClasses);
	}

	return references;
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception
    {
	LoginResult loginResult = LoginUtil.loginToSalesforce("kiwionly@gmail.com", "7Virtual_23TqhuSLUFsxX8aYL8jrNoy57v", "https://login.salesforce.com/services/Soap/c/32");
	// LoginResult loginResult =
	// LoginUtil.loginToSalesforce("gheewooi.ong@domain.com.dev", "",
	// "https://test.salesforce.com/services/Soap/c/32");
	// LoginResult loginResult =
	// LoginUtil.loginToSalesforce("gheewooi.ong@domain.com.test", "",
	// "https://test.salesforce.com/services/Soap/c/32");
	VFPageScanner<?> scanner = new VFPageScanner(loginResult);

	List<String> queryPage = new ArrayList<String>();
	queryPage.add("Global_Log_HelloController ");
	queryPage.add("SRS_JobTrk_PrintLabelController ");
	queryPage.add("Account ");

	scanner.getReferencePage(queryPage);

	scanner.shutdown();
    }

}
