package com.github.autoforce.task.compile;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.github.autoforce.parser.ErrorObject;
import com.github.autoforce.parser.Parser;
import com.google.common.io.BaseEncoding;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.tooling.sobject.ApexClass;
import com.sforce.soap.tooling.sobject.ApexComponent;
import com.sforce.soap.tooling.sobject.ApexPage;
import com.sforce.soap.tooling.sobject.ApexTrigger;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("serial")
@WebServlet("/compile.do")
public class CompileController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String id = request.getParameter("id");
	String name = request.getParameter("name");
	String type = request.getParameter("type");

	HttpSession session = request.getSession();
	ToolingConnection conn = (ToolingConnection) session.getAttribute("tooling");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	Long start = System.currentTimeMillis();

	String code = getCode(conn, id, type);

	Long end = System.currentTimeMillis();

	long time = end - start;

	request.setAttribute("time", time / 1000.000);
	request.setAttribute("code", BaseEncoding.base64().encode(code.getBytes()));
	request.setAttribute("name", name);
	request.setAttribute("type", type);
	request.setAttribute("id", id);
	request.setAttribute("last", getTime(conn, type, id));
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/compile_form.jsp");
	dispatcher.forward(request, response);
    }

    public String getFullURL(HttpServletRequest request)
    {
	StringBuffer requestURL = request.getRequestURL();
	String queryString = request.getQueryString();

	if (queryString == null)
	{
	    return requestURL.toString();
	}
	else
	{
	    return requestURL.append("?").append(queryString).toString();
	}
    }

    private boolean isModified(ToolingConnection conn, String lastModified, String type, String id)
    {
	QueryResult result = null;

	try
	{
	    result = conn.query("select id, LastModifiedDate from " + type + " where id = '" + id + "' limit 1");
	}
	catch (ConnectionException e)
	{
	    e.printStackTrace();
	    return false;
	}

	SObject[] records = result.getRecords();

	if (records.length <= 0)
	    return false;

	Calendar cal = null;

	if (type.equals("ApexClass"))
	{
	    ApexClass clazz = (ApexClass) records[0];

	    cal = clazz.getLastModifiedDate();
	}
	else
	    if (type.equals("ApexTrigger"))
	    {
		ApexTrigger clazz = (ApexTrigger) records[0];

		cal = clazz.getLastModifiedDate();
	    }
	    else
		if (type.equals("ApexPage"))
		{
		    ApexPage clazz = (ApexPage) records[0];

		    cal = clazz.getLastModifiedDate();
		}
		else
		    if (type.equals("ApexComponent"))
		    {
			ApexComponent clazz = (ApexComponent) records[0];

			cal = clazz.getLastModifiedDate();
		    }

	final Date currentTime = cal.getTime();
	final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String codeTime = sdf.format(currentTime);

	Date date = toDate(lastModified);
	Date last = toDate(codeTime);

	Long myLastModifiedDate = date.getTime();
	Long LastModifiedDate = last.getTime();

	if (myLastModifiedDate < LastModifiedDate)
	{
	    return true;
	}

	return false;
    }

    public Date toDate(String time)
    {
	Date date = null;

	try
	{
	    date = new SimpleDateFormat("yyyy-M-d HH:mm:ss").parse(time);
	}
	catch (ParseException e)
	{
	    return null;
	}

	return date;
    }

    private String getTime(ToolingConnection conn, String type, String id)
    {
	QueryResult result = null;

	try
	{
	    result = conn.query("select id, LastModifiedDate from  " + type + " where id = '" + id + "' limit 1");
	}
	catch (ConnectionException e)
	{
	    e.printStackTrace();
	    return null;
	}

	SObject[] records = result.getRecords();

	if (records.length <= 0)
	    return "";

	Calendar cal = null;

	if (type.equals("ApexClass"))
	{
	    ApexClass clazz = (ApexClass) records[0];

	    cal = clazz.getLastModifiedDate();
	}
	else
	    if (type.equals("ApexTrigger"))
	    {
		ApexTrigger clazz = (ApexTrigger) records[0];

		cal = clazz.getLastModifiedDate();
	    }
	    else
		if (type.equals("ApexPage"))
		{
		    ApexPage clazz = (ApexPage) records[0];

		    cal = clazz.getLastModifiedDate();
		}
		else
		    if (type.equals("ApexComponent"))
		    {
			ApexComponent clazz = (ApexComponent) records[0];

			cal = clazz.getLastModifiedDate();
		    }

	final Date currentTime = cal.getTime();
	final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String now = sdf.format(currentTime);

	return now;
    }

    private String getCode(ToolingConnection conn, String id, String type)
    {
	ApexCodeReader reader = new ApexCodeReader();

	String result = "";

	try
	{

	    if (type.equals("ApexClass"))
	    {
		result = reader.getClass(conn, id);
	    }
	    else
		if (type.equals("ApexPage"))
		{
		    result = reader.getPage(conn, id);
		}
		else
		    if (type.equals("ApexComponent"))
		    {
			result = reader.getComponent(conn, id);
		    }
		    else
			if (type.equals("ApexTrigger"))
			{
			    result = reader.getTrigger(conn, id);
			}

	}
	catch (ConnectionException e)
	{
	    e.printStackTrace();
	}

	return result;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	Writer out = response.getWriter();

	String code = request.getParameter("code");
	String type = request.getParameter("type");
	String id = request.getParameter("id");
	String checkModified = request.getParameter("checked");
	String lastModified = request.getParameter("lastModified");

	HttpSession session = request.getSession();
	com.sforce.soap.apex.SoapConnection conn = (com.sforce.soap.apex.SoapConnection) session.getAttribute("apex");
	EnterpriseConnection enterprise = (EnterpriseConnection) session.getAttribute("enterprise");
	ToolingConnection tool = (ToolingConnection) session.getAttribute("tooling");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	// put u filter here
	if (code.indexOf("isTestRunning()") != -1)
	{
	    Map<String, String> result = new HashMap<String, String>();

	    result.put("line", "" + 0);
	    result.put("time", "" + -1);
	    result.put("message", "isTestRunning won't work here :) ");

	    String json = JSON.toJSONString(result);

	    out.write(json);

	    out.close();
	}

	// included apex parse for check apex syntax before send to salesforce
	/*
	 * Map<String, String> compileResult;
	 * 
	 * try { compileResult = parse(code); } catch (Exception ex) {
	 * ex.printStackTrace(); return; }
	 * 
	 * if(!compileResult.isEmpty()) { String json =
	 * JSON.toJSONString(compileResult);
	 * 
	 * out.write(json);
	 * 
	 * return; }
	 */
	// end apex parse

	CompileTask compiler = new CompileTask();

	Map<String, String> result = new HashMap<String, String>();

	Boolean mod = Boolean.parseBoolean(checkModified);

	if (mod)
	{
	    if (isModified(tool, lastModified, type, id))
	    {
		result.put("line", "-1");
		result.put("time", "-1");
		result.put("message", "some one had update the source, please save your work and refresh to modify again");

		String json = JSON.toJSONString(result);
		out.write(json);
		out.close();

		return;
	    }
	}

	try
	{

	    if (type.equals("ApexTrigger"))
		result = compiler.compileTrigger(conn, code);
	    else
		if (type.equals("ApexClass"))
		    result = compiler.compile(conn, code);
		else
		    if (type.equals("ApexComponent"))
			result = compiler.compileComponent(enterprise, code, id);
		    else
			result = compiler.compilePage(enterprise, code, id);

	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    result.put("line", "-1");
	    result.put("time", "-1");
	    result.put("message", e.getMessage());

	    String json = JSON.toJSONString(result);

	    out.write(json);

	    out.close();

	    return;
	}

	String json = JSON.toJSONString(result);

	out.write(json);

	out.close();
    }

    public Map<String, String> parse(String code) throws Exception
    {
	Map<String, String> result = new HashMap<String, String>();

	long start = System.currentTimeMillis();

	Parser parser = new Parser(code);

	long stop = System.currentTimeMillis();

	long time = stop - start;

	Map<String, Object> compileResult = parser.parse();

	if (compileResult.isEmpty())
	{
	    return result;
	}

	// get error here
	Set<String> key = compileResult.keySet();

	for (String k : key)
	{
	    @SuppressWarnings("unchecked")
	    Map<String, Object> error = (Map<String, Object>) compileResult.get(k);

	    Set<String> er = error.keySet();

	    for (String ke : er)
	    {
		ErrorObject errorObject = (ErrorObject) error.get(ke);

		result.put("line", "" + errorObject.getLine());
		result.put("time", "" + time);
		result.put("message", "Fail compile Class: " + errorObject.getMessage());
	    }
	}

	return result;
    }

}
