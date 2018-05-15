package com.github.autoforce.task.compile;

import java.util.ArrayList;
import java.util.List;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class ApexCodeReader
{
    public List<ApexCode> getCodeCoverage(ToolingConnection conn, String type, String filter, String file) throws Exception
    {
	List<ApexCode> typeList;

	if (type.equals("ApexClass"))
	    typeList = getClazzList(conn, filter);
	else
	    typeList = getTriggerList(conn, filter);

	return typeList;
    }

    public List<ApexCode> getClazzList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<ApexCode> clazzList = new ArrayList<ApexCode>();

	String soql = "SELECT id, name, ApiVersion, Status FROM ApexClass where name like '" + filter + "%' order by name";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexClass clazz = (ApexClass) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    clazzList.add(new ApexCode(id, name, "ApexClass"));

	}

	return clazzList;
    }

    public List<ApexCode> getTriggerList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<ApexCode> clazzList = new ArrayList<ApexCode>();

	String soql = "SELECT id, name, ApiVersion, Status FROM ApexTrigger where name like '" + filter + "%' order by name";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexTrigger clazz = (ApexTrigger) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    clazzList.add(new ApexCode(id, name, "ApexTrigger"));
	}

	return clazzList;
    }

    public List<ApexCode> getPageList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<ApexCode> clazzList = new ArrayList<ApexCode>();

	String soql = "SELECT id, name, apiVersion FROM ApexPage where name like '" + filter + "%' order by name";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexPage clazz = (ApexPage) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    clazzList.add(new ApexCode(id, name, "ApexPage"));
	}

	return clazzList;
    }

    public List<ApexCode> getComponentList(ToolingConnection conn, String filter) throws ConnectionException
    {
	List<ApexCode> clazzList = new ArrayList<ApexCode>();

	String soql = "SELECT id, name, apiVersion FROM ApexComponent where name like '" + filter + "%' order by name";
	QueryResult result = conn.query(soql);

	for (SObject object : result.getRecords())
	{

	    ApexComponent clazz = (ApexComponent) object;

	    String id = clazz.getId();
	    String name = clazz.getName();

	    clazzList.add(new ApexCode(id, name, "ApexPage"));
	}

	return clazzList;
    }

    public String getClass(ToolingConnection conn, String id) throws ConnectionException
    {
	String soql = "SELECT id, Body, name, ApiVersion, Status FROM ApexClass where id = '" + id + "' ";
	QueryResult result = conn.query(soql);

	ApexClass clazz = (ApexClass) result.getRecords()[0];

	String body = clazz.getBody();

	return body;
    }

    public String getPage(ToolingConnection conn, String id) throws ConnectionException
    {
	String soql = "SELECT id, name, Markup, apiVersion FROM ApexPage where id = '" + id + "' ";
	QueryResult result = conn.query(soql);

	ApexPage clazz = (ApexPage) result.getRecords()[0];

	String body = clazz.getMarkup();

	return body;
    }

    public String getComponent(ToolingConnection conn, String id) throws ConnectionException
    {
	String soql = "SELECT id, name, Markup, apiVersion FROM ApexComponent where id = '" + id + "' ";
	QueryResult result = conn.query(soql);

	ApexComponent clazz = (ApexComponent) result.getRecords()[0];

	String body = clazz.getMarkup();

	return body;
    }

    public String getTrigger(ToolingConnection conn, String id) throws ConnectionException
    {
	String soql = "SELECT id, Body, name, ApiVersion, Status FROM ApexTrigger where id = '" + id + "' ";
	QueryResult result = conn.query(soql);

	ApexTrigger clazz = (ApexTrigger) result.getRecords()[0];

	String body = clazz.getBody();

	return body;
    }

    public static class ApexCode
    {
	private final String id;
	private final String name;
	private final String type;

	public ApexCode(String id, String name, String type)
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

	@Override
	public String toString()
	{
	    return "ApexCode [id=" + id + ", name=" + name + ", type=" + type + "]";
	}

    }

    public static ToolingConnection login() throws ConnectionException
    {
	final String USERNAME = "gheewooi.ong@domain.com.csodev1";
	final String PASSWORD = "";
	final String URL = "https://domain--csodev1.cs1.my.salesforce.com/services/Soap/c/31.0";

	final LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return LoginUtil.createToolingConnection(loginResult);
    }

    public static void main(String[] args) throws Exception
    {
	long start = System.currentTimeMillis();

	ApexCodeReader reader = new ApexCodeReader();

	List<ApexCode> result = reader.getPageList(login(), "SRS");

	for (ApexCode apexCode : result)
	{
	    System.out.println(apexCode);
	}

	long end = System.currentTimeMillis();

	System.out.println(end - start);

    }

}
