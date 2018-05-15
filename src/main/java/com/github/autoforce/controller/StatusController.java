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
import com.sforce.soap.tooling.ToolingConnection;

@SuppressWarnings("serial")
@WebServlet("/status.do")
public class StatusController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	HttpSession session = request.getSession();
	ToolingConnection conn = (ToolingConnection) session.getAttribute("tooling");

	if (conn != null)
	{
	    request.setAttribute("sessionId", LoginUtil.getSessionId());
	    request.setAttribute("serverUrl", LoginUtil.getServerUrl());
	}

	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/status.jsp");
	dispatcher.forward(request, response);
    }
}
