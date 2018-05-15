package com.github.autoforce.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.reflections.Reflections;

@SuppressWarnings("serial")
@WebServlet("/task.do")
public class TaskController extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
	List<Task> list = new ArrayList<Task>();

	Reflections reflections = new Reflections("com.github.autoforce.task");
	Set<Class<?>> clazzes = reflections.getTypesAnnotatedWith(WebServlet.class);

	for (Class<?> class1 : clazzes)
	{
	    Annotation[] anns = class1.getAnnotations();

	    for (Annotation annotation : anns)
	    {
		if (annotation instanceof Controller)
		{
		    Controller con = (Controller) annotation;

		    String name = con.name();
		    String desc = con.description();
		    String url = con.url();

		    Task task = new Task(url, name, desc);

		    list.add(task);
		}

	    }
	}

	Collections.sort(list, new Comparator<Task>() {

	    @Override
	    public int compare(Task o1, Task o2)
	    {
		return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	    }
	});

	request.setAttribute("taskList", list);

	RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/task.jsp");
	dispatcher.forward(request, response);
    }
}
