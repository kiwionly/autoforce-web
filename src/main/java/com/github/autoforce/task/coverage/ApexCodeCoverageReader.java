package com.github.autoforce.task.coverage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.autoforce.LoginUtil;
import com.github.autoforce.task.coverage.Records.Coverage;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class ApexCodeCoverageReader
{
    public List<TestResult> getCodeCoverage(ToolingConnection conn, String type, String filter, String file) throws Exception
    {
	long start = System.currentTimeMillis();

	List<KeyValuePair> typeList;

	if (type.equals("ApexClass"))
	    typeList = getClazzList(conn, filter);
	else
	    typeList = getTriggerList(conn, filter);

	String queryId = createQueryIds(typeList);

	QueryResultProcessor test = new QueryResultProcessor();

	String query = "SELECT ApexTestClassId, ApexClassorTriggerId, TestMethodName, NumLinesCovered, NumLinesUncovered, Coverage FROM ApexCodeCoverage WHERE ApexClassOrTriggerId in (" + queryId
		+ ")";

	String domain = getDomain(LoginUtil.getServerUrl());

	String sessionid = LoginUtil.getSessionId();
	List<Records> record = test.query(domain + "/services/Soap/T/33.0", sessionid, query, file);

	for (Records rec : record)
	{
	    for (KeyValuePair kv : typeList)
	    {
		String id = kv.getId();

		if (id.equals(rec.getApexClassOrTriggerId()))
		{
		    rec.setApexClassName(kv.getName());
		}
	    }
	}

	long end = System.currentTimeMillis();

	System.out.println(end - start);

	return processResult(record, typeList);
    }

    private String getDomain(String serverUrl)
    {
	int index = serverUrl.indexOf('/', 8);
	String sub = serverUrl.substring(0, index);

	return sub;
    }

    private List<Records> toRecords(QueryResult result)
    {
	List<Records> record = new ArrayList<Records>();
	SObject[] rec = result.getRecords();

	for (SObject sObject : rec)
	{

	    Records recc = new Records();

	    ApexCodeCoverage cov = (ApexCodeCoverage) sObject;
	    recc.setApexClassOrTriggerId(cov.getApexClassOrTriggerId());
	    recc.setApexTestClassId(cov.getApexTestClassId());
	    recc.setNumLinesCovered(cov.getNumLinesCovered());
	    recc.setNumLinesUncovered(cov.getNumLinesUncovered());
	    recc.setTestMethodName(cov.getTestMethodName());
	    recc.addCoveredLines(cov.getCoverage().getCoveredLines().length);
	    recc.addUncoveredLines(cov.getCoverage().getUncoveredLines().length);

	    record.add(recc);
	}

	return record;
    }

    private List<TestResult> processResult(List<Records> recordList, List<KeyValuePair> clazzList)
    {
	List<TestResult> result = new ArrayList<TestResult>();

	List<LineCoverage> codeCoverage = getCoverage(recordList);

	for (KeyValuePair kv : clazzList)
	{
	    String id = kv.getId();
	    String name = kv.getName();
	    String type = kv.getType();

	    Set<Integer> allcover = new HashSet<Integer>();
	    Set<Integer> alluncover = new HashSet<Integer>();

	    for (Records rec : recordList)
	    {
		if (id.equals(rec.getApexClassOrTriggerId()))
		{

		    Set<Integer> cover = rec.getCoverage().getCoveredLines();
		    allcover.addAll(cover);

		    Set<Integer> uncover = rec.getCoverage().getUncoveredLines();
		    alluncover.addAll(uncover);
		}
	    }

	    alluncover.removeAll(allcover);

	    double percent = 0;

	    if (!(alluncover.isEmpty() && allcover.isEmpty()))
		percent = (allcover.size() / ((double) allcover.size() + (double) alluncover.size())) * 100;

	    LineCoverage lineCoverage = null;

	    for (LineCoverage lc : codeCoverage)
	    {
		if (id.equals(lc.getClassName()))
		{
		    lineCoverage = lc;
		}
	    }

	    result.add(new TestResult(id, name, percent, type, lineCoverage));
	}
	System.out.println(result);
	return result;
    }

    private String createQueryIds(List<KeyValuePair> clazzList)
    {

	if (clazzList.isEmpty())
	    return "";

	StringBuilder buf = new StringBuilder();

	for (KeyValuePair id : clazzList)
	{
	    buf.append("'");
	    buf.append(id.getId());
	    buf.append("'");
	    buf.append(",");
	}

	String allId = buf.toString();
	allId = allId.substring(0, allId.length() - 1);

	System.out.println("all id -> " + allId);
	return allId;
    }

    private List<KeyValuePair> getClazzList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<KeyValuePair> clazzList = new ArrayList<KeyValuePair>();

	String soql = "SELECT id, Body, name, ApiVersion, Status FROM ApexClass where name like '" + filter + "%' ";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexClass clazz = (ApexClass) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    if (name.contains("Global_Test"))
	    {
		clazzList.add(new KeyValuePair(id, name, "ApexClass"));
	    }

	    if (!(name.contains("Test") || name.contains("test")))
	    {
		clazzList.add(new KeyValuePair(id, name, "ApexClass"));
	    }
	}

	return clazzList;
    }

    private List<KeyValuePair> getTriggerList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<KeyValuePair> clazzList = new ArrayList<KeyValuePair>();

	String soql = "SELECT id, Body, name, ApiVersion, Status FROM ApexTrigger where name like '" + filter + "%' ";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexTrigger clazz = (ApexTrigger) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    if (!(name.contains("Test") || name.contains("test")))
	    {
		clazzList.add(new KeyValuePair(id, name, "ApexTrigger"));
	    }
	}

	return clazzList;
    }

    public List<LineCoverage> getCoverage(List<Records> records)
    {
	List<LineCoverage> coverage = new ArrayList<LineCoverage>();
	Set<String> ids = new HashSet<String>();

	for (Records rec : records)
	{
	    LineCoverage cov = new LineCoverage();
	    cov.setClassName(rec.getApexClassOrTriggerId());

	    if (!ids.contains(rec.getApexClassOrTriggerId()))
	    {
		for (Records com : records)
		{

		    if (rec.getApexClassOrTriggerId().equals(com.getApexClassOrTriggerId()))
		    {
			Coverage c = com.getCoverage();

			for (int lin : c.getCoveredLines())
			{
			    cov.addCoverage(lin);
			}

			for (int lin : c.getUncoveredLines())
			{
			    cov.addUncoverage(lin);
			}

		    }
		}

		cov.getUncoverage().removeAll(cov.getCoverage());

		coverage.add(cov);
	    }

	    ids.add(rec.getApexClassOrTriggerId());
	}

	return coverage;
    }

    private static class KeyValuePair
    {
	private final String id;
	private final String name;
	private final String type;

	public KeyValuePair(String id, String name, String type)
	{
	    this.id = id;
	    this.name = name;
	    this.type = type;
	}

	public String getId()
	{
	    return id;
	}

	public String getName()
	{
	    return name;
	}

	public String getType()
	{
	    return type;
	}
    }

    public static class TestResult
    {
	private final String id;
	private final String name;
	private final double percent;
	private final String type;
	private final LineCoverage coverage;

	public TestResult(String id, String name, double percent, String type, LineCoverage coverage)
	{
	    this.id = id;
	    this.name = name;
	    this.percent = percent;
	    this.type = type;
	    this.coverage = coverage;
	}

	public String getId()
	{
	    return id;
	}

	public String getName()
	{
	    return name;
	}

	public double getPercent()
	{
	    return percent;
	}

	public LineCoverage getCoverage()
	{
	    return coverage;
	}

	@Override
	public String toString()
	{
	    return "TestResult [id=" + id + ", name=" + name + ", percent=" + percent + ", type=" + type + ", coverage=" + coverage + "]";
	}

    }

    public class LineCoverage
    {
	private String className;
	private Set<Integer> coverage = new HashSet<Integer>();
	private Set<Integer> uncoverage = new HashSet<Integer>();

	public String getClassName()
	{
	    return className;
	}

	public void setClassName(String className)
	{
	    this.className = className;
	}

	public Set<Integer> getCoverage()
	{
	    return coverage;
	}

	public void addCoverage(int coverage)
	{
	    this.coverage.add(coverage);
	}

	public Set<Integer> getUncoverage()
	{
	    return uncoverage;
	}

	public String getCoverageString()
	{
	    return toString(coverage);
	}

	public String getUncoverageString()
	{
	    return toString(uncoverage);
	}

	public String toString(Set<Integer> set)
	{
	    if (set.isEmpty())
		return "";

	    StringBuilder buf = new StringBuilder();

	    for (Integer line : set)
	    {
		buf.append(line);
		buf.append(",");
	    }

	    String all = buf.toString();
	    all = all.substring(0, all.length() - 1);

	    return all;
	}

	public void addUncoverage(int uncoverage)
	{
	    this.uncoverage.add(uncoverage);
	}

	@Override
	public String toString()
	{
	    return "LineCoverage [className=" + className + ", coverage=" + coverage + ", uncoverage=" + uncoverage + "]";
	}

    }

    public static ToolingConnection login() throws ConnectionException
    {
	final String USERNAME = "gheewooi.ong@domain.com.test";
	final String PASSWORD = "";
	final String URL = "https://domain--Test.cs30.my.salesforce.com/services/Soap/c/32.0";

	final LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return LoginUtil.createToolingConnection(loginResult);
    }

    public static void main(String[] args) throws Exception
    {
	ApexCodeCoverageReader reader = new ApexCodeCoverageReader();

	List<TestResult> result = reader.getCodeCoverage(login(), "ApexTrigger", "SRS_", "xml/request.xml");
	System.out.println("clazz size = " + result.size());
	for (TestResult testResult : result)
	{
	    System.out.println(testResult);
	}

	// System.out.println(result.getSize());
	//
	// for (SObject object : result.getRecords()) {
	//
	// ApexCodeCoverage acc = (ApexCodeCoverage)object;
	//
	// double covered = acc.getNumLinesCovered();
	// double uncovered = acc.getNumLinesUncovered();
	//
	// double percent = (covered / (covered + uncovered)) * 100;
	//
	// System.out.println(acc.getApexClassOrTriggerId() + " -> " +
	// acc.getApexTestClassId() + " -> " + acc.getTestMethodName() + " covered " +
	// percent + "%.");
	// }

    }

}
