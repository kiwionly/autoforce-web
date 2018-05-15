package com.github.autoforce.task.execute;

import java.io.File;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.LoginUtil;
import com.github.autoforce.controller.Controller;
import com.sforce.soap.apex.SoapConnection;

@Controller(name = "Execute Apex Code", description = "run apex code", url = "execute.jsp")
@SuppressWarnings("serial")
@WebServlet("/execute.do")
public class ExecuteController extends HttpServlet
{

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String nextJSP = "/execute.jsp";
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
	dispatcher.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String code = request.getParameter("code");
	String level = request.getParameter("level");

	HttpSession session = request.getSession();
	SoapConnection conn = (SoapConnection) session.getAttribute("apex");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	String serverUrl = LoginUtil.getServerUrl(LoginUtil.getServerUrl(), "/s/");

	String sessionId = LoginUtil.getSessionId();

	Long start = System.currentTimeMillis();

	ExecuteAnonymousProcessor pro = new ExecuteAnonymousProcessor();

	String absoluteDiskPath = getServletContext().getRealPath("/");

	File file = new File(absoluteDiskPath + File.separator + "xml/execute.xml");

	Result result = pro.query(serverUrl, sessionId, file, "apex_code", level, code);

	Long end = System.currentTimeMillis();

	long time = end - start;

	if (result == null)
	    return;

	if (!result.getCompileProblem().isEmpty())
	{
	    request.setAttribute("error", replaceHtmlEntities(result.getCompileProblem()));
	}

	if (!result.getExceptionMessage().isEmpty())
	{
	    request.setAttribute("error", replaceHtmlEntities(result.getExceptionMessage()));
	}

	request.setAttribute("time", time / 1000.000);
	request.setAttribute("code", code);
	request.setAttribute("result", replaceHtmlEntities(result.getDebugLog()));
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/execute.jsp");
	dispatcher.forward(request, response);
    }

    private String replaceNewLinewithBr(String debugLog)
    {
	String newLine = debugLog.replace("\n", "<br />");

	return newLine;
    }

    private String replaceHtmlEntities(String debugLog)
    {
	String newLine = debugLog.replace("<", "&lt;");
	newLine = newLine.replace(">", "&gt;");

	return replaceNewLinewithBr(newLine);
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
