package com.github.autoforce.task.compile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.LoginUtil;
import com.github.autoforce.controller.Controller;
import com.github.autoforce.task.compile.ApexCodeReader.ApexCode;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

@Controller(name = "Compile Code", description = "immortal web IDE that replace original salesforce web IDE \"lock by admin\" error", url = "apex_form.jsp")
@SuppressWarnings("serial")
@WebServlet("/apexCode.do")
public class ListApexController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String prefix = request.getParameter("prefix");
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

	List<ApexCode> result = list(conn, type, prefix);

	Long end = System.currentTimeMillis();

	long time = end - start;

	String url = LoginUtil.getServerUrl();
	int index = url.indexOf("/", "https://".length());
	url = url.substring(0, index);

	request.setAttribute("time", time / 1000.000);
	request.setAttribute("server", url);
	request.setAttribute("type", type);
	request.setAttribute("result", result);
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/apex.jsp");
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

    private List<ApexCode> list(ToolingConnection conn, String type, String prefix)
    {
	ApexCodeReader reader = new ApexCodeReader();

	List<ApexCode> result = new ArrayList<ApexCode>();

	try
	{

	    if (type.equals("ApexClass"))
	    {
		result = reader.getClazzList(conn, prefix);
	    }
	    else
		if (type.equals("ApexPage"))
		{
		    result = reader.getPageList(conn, prefix);
		}
		else
		    if (type.equals("ApexTrigger"))
		    {
			result = reader.getTriggerList(conn, prefix);
		    }
		    else
			if (type.equals("ApexComponent"))
			{
			    result = reader.getComponentList(conn, prefix);
			}

	}
	catch (ConnectionException e)
	{
	    e.printStackTrace();
	}

	return result;
    }
}
