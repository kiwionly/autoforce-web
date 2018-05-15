package com.github.autoforce.task.genpack;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.autoforce.controller.Controller;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.MetadataConnection;

@Controller(name = "Package Xml generator", description = "This tool use to generate package xml", url = "generate_form.do")
@SuppressWarnings("serial")
@WebServlet("/package.do")
public class GeneratorController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	String pre = request.getParameter("prefix");

	Set<String> prefix = new HashSet<String>();
	for (String token : pre.split(","))
	{
	    prefix.add(token.trim());
	}

	String[] mt = request.getParameterValues("metaTypes");

	Set<String> metaTypes = new HashSet<String>();
	for (String meta : mt)
	{
	    metaTypes.add(meta.trim());
	}

	Set<String> excludes = new HashSet<String>();
	String exclude = request.getParameter("exclude");

	if (exclude != null)
	{
	    for (String token : exclude.split(","))
	    {
		excludes.add(token.trim());
	    }
	}

	HttpSession session = request.getSession();

	MetadataConnection conn = (MetadataConnection) session.getAttribute("metadata");

	if (conn == null)
	{
	    session.setAttribute("lastUrl", getFullURL(request));
	    response.sendRedirect("login.jsp");
	    return;
	}

	Long start = System.currentTimeMillis();

	String xml = generate(conn, prefix, metaTypes, excludes);

	Long end = System.currentTimeMillis();

	long time = end - start;

	System.out.println(time);

	Writer writer = response.getWriter();

	response.setContentType("text/xml");

	writer.write(xml);
	writer.close();

	// request.setAttribute("time", time / 1000.000);
	// request.setAttribute("xml", xml);
	// RequestDispatcher dispatcher =
	// getServletContext().getRequestDispatcher("/generate.jsp");
	// dispatcher.forward(request,response);
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

    private String generate(MetadataConnection conn, Set<String> prefix, Set<String> metaTypes, Set<String> excludes)
    {
	MetadataReader reader = new MetadataReader();

	List<FileProperties> fp = reader.getFileList(conn, metaTypes, 31.0);

	List<FileProperties> filterList = reader.filter(prefix, fp, excludes);

	PackageXmlGenerator gen = new PackageXmlGenerator();
	String content = gen.generate(metaTypes, filterList, 32.0);

	reader.shutdown();

	return content;
    }
}
