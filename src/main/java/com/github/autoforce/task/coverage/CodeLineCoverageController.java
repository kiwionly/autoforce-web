package com.github.autoforce.task.coverage;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.task.compile.ApexCodeReader;
import com.google.common.io.BaseEncoding;
import com.sforce.soap.tooling.ToolingConnection;

@SuppressWarnings("serial")
@WebServlet("/codecoverage.do")
public class CodeLineCoverageController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String covered = request.getParameter("covered");
	String uncovered = request.getParameter("uncovered");
	String type = request.getParameter("type");
	String name = request.getParameter("name");
	String id = request.getParameter("id");

	HttpSession session = request.getSession();
	ToolingConnection conn = (ToolingConnection) session.getAttribute("tooling");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	Long start = System.currentTimeMillis();

	ApexCodeReader apex = new ApexCodeReader();

	String code = null;

	try
	{

	    if (type.equals("ApexClass"))
	    {
		code = apex.getClass(conn, id);
	    }
	    else
	    {
		code = apex.getTrigger(conn, id);
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	request.setAttribute("code", BaseEncoding.base64().encode(code.getBytes()));

	Long end = System.currentTimeMillis();

	long time = end - start;

	request.setAttribute("covered", covered);
	request.setAttribute("uncovered", uncovered);
	request.setAttribute("type", type);
	request.setAttribute("name", name);
	request.setAttribute("id", id);
	request.setAttribute("time", time / 1000.000);

	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/codecoverage.jsp");
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
