package com.github.autoforce.task.coverage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.controller.Controller;
import com.github.autoforce.task.coverage.ApexCodeCoverageReader.TestResult;
import com.sforce.soap.tooling.ToolingConnection;

@Controller(name = "Code Coverage", description = "showing code coverage in old salesforce style", url = "coverage_form.jsp")
@SuppressWarnings("serial")
@WebServlet("/coverage.do")
public class CodeCoverageController extends HttpServlet
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

	List<TestResult> result = generate(conn, type, prefix);

	Long end = System.currentTimeMillis();

	long time = end - start;

	request.setAttribute("time", time / 1000.000);
	request.setAttribute("result", result);
	request.setAttribute("type", type);
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/coverage.jsp");
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

    private List<TestResult> generate(ToolingConnection conn, String type, String prefix)
    {
	ApexCodeCoverageReader reader = new ApexCodeCoverageReader();

	List<TestResult> result = null;

	String absoluteDiskPath = getServletContext().getRealPath("/");

	File file = new File(absoluteDiskPath + File.separator + "xml/request.xml");

	try
	{
	    result = reader.getCodeCoverage(conn, type, prefix, file.getAbsolutePath());
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return result;
    }
}
