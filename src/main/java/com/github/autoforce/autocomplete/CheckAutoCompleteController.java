package com.github.autoforce.autocomplete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("serial")
@WebServlet("/checkAutoComplete.do")
public class CheckAutoCompleteController extends HttpServlet
{
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	Writer out = response.getWriter();

	String code = request.getParameter("code");
	String cursor = request.getParameter("cursor");
	int row = Integer.parseInt(cursor);

	String variable = getToCompleteVar(code, row);
	String type = findType(variable, code);

	System.out.println("type = " + type);

	if (type == null || type.isEmpty())
	    return;

	HttpSession session = request.getSession();
	ToolingConnection tooling = (ToolingConnection) session.getAttribute("tooling");
	EnterpriseConnection enterprise = (EnterpriseConnection) session.getAttribute("enterprise");

	Autocompleter auto = new Autocompleter(tooling, enterprise);

	long start = System.currentTimeMillis();

	List<String> list = new ArrayList<String>();

	try
	{
	    list = auto.getSuggestText(type);
	}
	catch (ConnectionException e)
	{
	    e.printStackTrace();

	    out.write(e.getMessage());
	    out.close();

	    return;
	}

	long end = System.currentTimeMillis();

	List<String> myList = new ArrayList<String>(list);
	Collections.sort(myList, new Comparator<String>() {

	    @Override
	    public int compare(String o1, String o2)
	    {
		return o1.compareTo(o2);
	    }
	});

	System.out.println(end - start);

	List<Map<String, String>> result = new ArrayList<Map<String, String>>();

	for (String value : myList)
	{
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("word", value);
	    map.put("type", type);

	    result.add(map);
	}

	String json = JSON.toJSONString(result);
	System.out.println(json);
	out.write(json);
	out.close();

	return;
    }

    private String findType(String variable, String code) throws IOException
    {
	BufferedReader buf = new BufferedReader(new StringReader(code));

	String line = null;
	String type = null;

	while ((line = buf.readLine()) != null)
	{
	    boolean isVar = checkVariableDeclaration(line);

	    if (!isVar)
		continue;

	    line = line.replace("{get;set;}", "");
	    line = line.replace("private", "");
	    line = line.replace("public", "");
	    line = line.replace("static", "");
	    line = line.replace("final", "");

	    if (contain(line, variable))
	    {
		System.out.println(line);

		line = line.replace(variable, "");
		line = line.replace(";", "");
		line = line.replace("", "");

		type = line;

		break;
	    }

	}

	buf.close();

	if (type != null)
	    return type.trim();

	return null;
    }

    private boolean contain(String line, String variable)
    {
	String[] tokens = line.split(" |=|,|;");

	for (String t : tokens)
	{

	    if (t.equals(variable))
		return true;
	}

	return false;
    }

    private boolean checkVariableDeclaration(String line)
    {
	String[] tokens = line.split(" |=|,|;");

	boolean isVar = false;

	Set<String> keywords = new HashSet<String>();
	keywords.add("private");
	keywords.add("public");
	keywords.add("static");
	keywords.add("final");
	keywords.add("{get;set;}");

	for (String t : tokens)
	{

	    if (!t.equals(""))
	    {
		if (keywords.contains(t))
		    isVar = true;
	    }
	}

	return isVar;
    }

    private String getToCompleteVar(String code, int row) throws IOException
    {
	BufferedReader buf = new BufferedReader(new StringReader(code));

	String line = null;
	String toCompleteLine = null;
	int i = 0;

	while ((line = buf.readLine()) != null)
	{
	    if (row == i)
	    {
		toCompleteLine = line;
		break;
	    }

	    i++;
	}

	buf.close();

	// try to get the variable

	if (toCompleteLine.isEmpty())
	    return null;

	String[] tokens = toCompleteLine.split(" |=");

	String var = tokens[tokens.length - 1];

	var = var.replace(".", "");

	return var;
    }

}
