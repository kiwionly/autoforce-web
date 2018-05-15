package com.github.autoforce.task.genpack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/generate_form.do")
public class GeneratorformController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	List<MetaType> types = getMetaTypes();

	request.setAttribute("type", types);
	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/generate_form.jsp");
	dispatcher.forward(request, response);
    }

    public class MetaType
    {
	private final String name;
	private final boolean checked;

	public MetaType(String name, boolean checked)
	{
	    this.name = name;
	    this.checked = checked;
	}

	public String getName()
	{
	    return name;
	}

	public boolean isChecked()
	{
	    return checked;
	}

	@Override
	public String toString()
	{
	    return "MetaType [name=" + name + ", checked=" + checked + "]";
	}
    }

    public List<MetaType> getMetaTypes()
    {
	List<MetaType> list = new ArrayList<MetaType>();

	list.add(new MetaType("ApexClass", true));
	list.add(new MetaType("ApexPage", true));
	list.add(new MetaType("ApexTrigger", true));
	list.add(new MetaType("ApexComponent", false));
	list.add(new MetaType("ApprovalProcess", false));
	list.add(new MetaType("CustomField", false));
	list.add(new MetaType("CustomLabel", false));
	list.add(new MetaType("CustomSite", false));
	list.add(new MetaType("CustomObject", false));
	list.add(new MetaType("CustomTab", false));
	list.add(new MetaType("CustomApplication", false));
	// list.add(new MetaType("EmailTemplate", false));
	// list.add(new MetaType("Group", false));
	list.add(new MetaType("Layout", false));
	list.add(new MetaType("ListView", false));
	list.add(new MetaType("PermissionSet", false));
	list.add(new MetaType("Queue", false));
	list.add(new MetaType("Profile", false));
	list.add(new MetaType("Report", false));
	// list.add(new MetaType("ReportType", false));
	list.add(new MetaType("StaticResource", false));
	list.add(new MetaType("Workflow", false));
	list.add(new MetaType("Flow", false));

	return list;
    }

}
