package com.github.autoforce.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/log.do")
public class LogController extends HttpServlet
{
    private WSClient client;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	if (client == null)
	{
	    client = WSClient.getWSClient();
	}

	InputStream log = request.getInputStream();

	client.send(toString(log));
    }

    private String toString(InputStream in) throws IOException
    {
	BufferedReader buf = new BufferedReader(new InputStreamReader(in));
	StringBuilder sb = new StringBuilder();
	String line = null;

	while ((line = buf.readLine()) != null)
	{
	    sb.append(line);
	}

	buf.close();

	return sb.toString();
    }

}
