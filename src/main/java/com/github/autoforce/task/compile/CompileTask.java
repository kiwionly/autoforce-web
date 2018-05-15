package com.github.autoforce.task.compile;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.ApexComponent;
import com.sforce.soap.enterprise.sobject.ApexPage;
import com.sforce.soap.enterprise.sobject.SObject;
import com.github.autoforce.LoginUtil;
import com.sforce.soap.apex.CompileClassResult;
import com.sforce.soap.apex.CompileTriggerResult;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.ws.ConnectionException;

public class CompileTask
{

    public Map<String, String> compile(SoapConnection conn, File file) throws Exception
    {
	String clazz = IOUtils.toString(new FileInputStream(file));

	return compile(conn, clazz);
    }

    public Map<String, String> compile(SoapConnection sforce, String clazz) throws Exception
    {
	String[] classes = { clazz };

	long start = System.currentTimeMillis();

	CompileClassResult[] compileResult = sforce.compileClasses(classes);

	long end = System.currentTimeMillis();

	long time = end - start;

	CompileClassResult compileClassResult = compileResult[0];

	if (compileClassResult.isSuccess())
	{

	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "" + compileClassResult.getLine());
	    result.put("time", "" + time);
	    result.put("message", "Successfully compile Class: " + compileClassResult.getId() + ".");

	    String now = getNow();

	    result.put("lastModified", now);

	    return result;
	}
	else
	{
	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "" + compileClassResult.getLine());
	    result.put("time", "" + time);
	    result.put("message", "Fail compile Class: " + compileClassResult.getProblem());

	    return result;
	}
    }

    private String getNow()
    {
	final Date currentTime = new Date();
	final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String now = sdf.format(currentTime);

	return now;
    }

    public Map<String, String> compilePage(EnterpriseConnection sforce, String clazz, String id) throws Exception
    {
	long start = System.currentTimeMillis();

	ApexPage page = new ApexPage();
	page.setId(id);
	page.setMarkup(clazz);

	List<SObject> list = new ArrayList<SObject>();
	list.add(page);

	SObject[] x = list.toArray(new SObject[list.size()]);
	SaveResult[] compileResult = sforce.update(x);

	long end = System.currentTimeMillis();

	long time = end - start;

	SaveResult saveResult = compileResult[0];

	if (saveResult.isSuccess())
	{

	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "-1");
	    result.put("time", "" + time);
	    result.put("message", "Successfully compile Page: " + saveResult.getId() + ".");

	    String now = getNow();

	    result.put("lastModified", now);

	    return result;
	}
	else
	{
	    Map<String, String> result = new HashMap<String, String>();

	    StringBuilder buf = new StringBuilder();
	    Error[] error = saveResult.getErrors();

	    for (Error err : error)
	    {
		buf.append(err.getMessage());
	    }

	    result.put("line", "-1");
	    result.put("time", "" + time);
	    result.put("message", "Fail compile Page: " + buf.toString());

	    return result;
	}
    }

    public Map<String, String> compileComponent(EnterpriseConnection sforce, String clazz, String id) throws Exception
    {
	long start = System.currentTimeMillis();

	ApexComponent page = new ApexComponent();
	page.setId(id);
	page.setMarkup(clazz);

	List<SObject> list = new ArrayList<SObject>();
	list.add(page);

	SObject[] x = list.toArray(new SObject[list.size()]);
	SaveResult[] compileResult = sforce.update(x);

	long end = System.currentTimeMillis();

	long time = end - start;

	SaveResult saveResult = compileResult[0];

	if (saveResult.isSuccess())
	{

	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "-1");
	    result.put("time", "" + time);
	    result.put("message", "Successfully compile Page: " + saveResult.getId() + ".");

	    String now = getNow();

	    result.put("lastModified", now);

	    return result;
	}
	else
	{
	    Map<String, String> result = new HashMap<String, String>();

	    StringBuilder buf = new StringBuilder();
	    Error[] error = saveResult.getErrors();

	    for (Error err : error)
	    {
		buf.append(err.getMessage());
	    }

	    result.put("line", "-1");
	    result.put("time", "" + time);
	    result.put("message", "Fail compile Page: " + buf.toString());

	    return result;
	}
    }

    public Map<String, String> compileTrigger(SoapConnection sforce, String clazz) throws Exception
    {
	String[] classes = { clazz };

	long start = System.currentTimeMillis();

	CompileTriggerResult[] compileResult = sforce.compileTriggers(classes);

	long end = System.currentTimeMillis();

	long time = end - start;

	CompileTriggerResult compileClassResult = compileResult[0];

	if (compileClassResult.isSuccess())
	{

	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "" + compileClassResult.getLine());
	    result.put("time", "" + time);
	    result.put("message", "Successfully compile Class: " + compileClassResult.getId() + ".");

	    String now = getNow();

	    result.put("lastModified", now);

	    return result;
	}
	else
	{
	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "" + compileClassResult.getLine());
	    result.put("time", "" + time);
	    result.put("message", "Fail compile Class: " + compileClassResult.getProblem());

	    return result;
	}
    }

    public static SoapConnection login() throws ConnectionException
    {
	final String USERNAME = "gheewooi.ong@domain.com.test";
	final String PASSWORD = "";
	final String URL = "https://domain--Test.cs30.my.salesforce.com/services/Soap/c/32.0";

	final LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return LoginUtil.createApexConnection(loginResult).getConnection();
    }

    public static void main(String[] args) throws Exception
    {
	CompileTask compiler = new CompileTask();

	SoapConnection conn = login();

	long start = System.currentTimeMillis();

	Map<String, String> result = compiler.compile(conn, new File("file.txt"));

	System.out.println(result);

	long end = System.currentTimeMillis();

	System.out.println(end - start);

	// compiler.compile(conn, new File("ap1.cls"));
	// compiler.compile(conn, new File("ap.cls"));

    }
}
