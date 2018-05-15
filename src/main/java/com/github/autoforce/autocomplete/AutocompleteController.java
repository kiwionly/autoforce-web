package com.github.autoforce.autocomplete;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@WebServlet("/autocomplete.do")
public class AutocompleteController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	Writer out = response.getWriter();

	String clazz = request.getParameter("clazz");

	HttpSession session = request.getSession();
	ToolingConnection tooling = (ToolingConnection) session.getAttribute("tooling");
	EnterpriseConnection enterprise = (EnterpriseConnection) session.getAttribute("enterprise");

	Autocompleter auto = new Autocompleter(tooling, enterprise);

	long start = System.currentTimeMillis();

	List<String> list = new ArrayList<String>();

	try
	{
	    list = auto.getSuggestText(clazz);
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

	    result.add(map);
	}

	String json = JSON.toJSONString(result);
	out.write(json);
	out.close();

    }

}
