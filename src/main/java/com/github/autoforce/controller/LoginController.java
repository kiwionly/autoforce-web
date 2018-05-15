package com.github.autoforce.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.fault.ExceptionCode;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("serial")
@WebServlet("/login.do")
public class LoginController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String error = request.getParameter("error");

	if (error != null)
	    request.setAttribute("error", error);

	String nextJSP = "/login.jsp";
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
	dispatcher.forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String email = request.getParameter("email");
	String password = request.getParameter("password");
	String instance = request.getParameter("instance");
	String version = request.getParameter("version");

	try
	{
	    LoginResult loginResult = LoginUtil.loginToSalesforce(email, password, instance + version);

	    MetadataConnection metadata = LoginUtil.createMetadataConnection(loginResult);
	    ToolingConnection tooling = LoginUtil.createToolingConnection(loginResult);
	    EnterpriseConnection connection = LoginUtil.createConnection(loginResult);
	    com.sforce.soap.apex.SoapConnection apex = LoginUtil.createApexConnection(loginResult).getConnection();
	    HttpSession session = request.getSession();

	    session.setAttribute("enterprise", connection);
	    session.setAttribute("tooling", tooling);
	    session.setAttribute("metadata", metadata);
	    session.setAttribute("apex", apex);

	}
	catch (LoginFault e)
	{
	    ExceptionCode code = e.getExceptionCode();
	    String message = e.getExceptionMessage();

	    String nextJSP = "login.do?error=" + code + "<br />" + message;
	    response.sendRedirect(nextJSP);

	    return;
	}
	catch (ConnectionException ex)
	{
	    ex.printStackTrace();
	}

	HttpSession session = request.getSession();

	String lastUrl = (String) session.getAttribute("lastUrl");

	if (lastUrl != null)
	{
	    response.sendRedirect(lastUrl);
	    session.removeAttribute("lastUrl");
	    return;
	}

	response.sendRedirect("task.do");
    }
}
