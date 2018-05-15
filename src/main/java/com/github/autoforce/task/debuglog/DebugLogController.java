package com.github.autoforce.task.debuglog;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.controller.Controller;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

@Controller(name = "Simple Debug Log", description = "show only System.debug", url = "debugLog.jsp")
@SuppressWarnings("serial")
@WebServlet("/debug.do")
public class DebugLogController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String n = request.getParameter("n");

	HttpSession session = request.getSession();
	ToolingConnection conn = (ToolingConnection) session.getAttribute("tooling");
	EnterpriseConnection enterprise = (EnterpriseConnection) session.getAttribute("enterprise");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	String id = (String) session.getAttribute("userId");

	if (id == null)
	{

	    try
	    {
		id = enterprise.getUserInfo().getUserId();
	    }
	    catch (ConnectionException e)
	    {
		e.printStackTrace();
	    }

	    session.setAttribute("userId", id);
	}

	Long start = System.currentTimeMillis();

	DebugLogConsole console = new DebugLogConsole();

	String log;

	try
	{
	    int limit = Integer.parseInt(n);

	    console.createTraceFlag(conn, id);

	    log = console.getDebugLog(conn, id, limit);
	}
	catch (Exception e)
	{
	    e.printStackTrace();

	    log = "something wrong had occurs !\n" + e.getMessage();
	}

	Long end = System.currentTimeMillis();

	long time = end - start;

	request.setAttribute("time", time / 1000.000);
	request.setAttribute("result", log);
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/debugLog.jsp");
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
	    return requestURL.append('?').append(queryString).toString();
	}
    }

}
