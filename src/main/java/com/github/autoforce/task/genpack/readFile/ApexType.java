package com.github.autoforce.task.genpack.readFile;

import java.io.FileReader;

import org.apache.commons.io.IOUtils;

import com.sforce.soap.tooling.sobject.ApexClass;
import com.sforce.soap.tooling.sobject.ApexPage;
import com.sforce.soap.tooling.sobject.ApexTrigger;
import com.sforce.soap.tooling.sobject.SObject;

public class ApexType
{
    private String name;
    private String body;
    private double version;
    private String status;

    public ApexType(SObject object)
    {
	if (object instanceof ApexClass)
	{
	    ApexClass apex = (ApexClass) object;

	    name = apex.getFullName();
	    body = apex.getBody();
	    version = apex.getApiVersion();
	    status = apex.getStatus();
	}
	else
	    if (object instanceof ApexTrigger)
	    {
		ApexTrigger apex = (ApexTrigger) object;

		name = apex.getFullName();
		body = apex.getBody();
		version = apex.getApiVersion();
		status = apex.getStatus();
	    }
	    else
		if (object instanceof ApexPage)
		{
		    ApexPage apex = (ApexPage) object;

		    name = apex.getName();
		    body = apex.getMarkup();
		    version = apex.getApiVersion();
		    status = null;
		}
    }

    public static String getColumn(String type)
    {
	if (type.equals("ApexClass"))
	    return "id, fullName, Body, apiVersion, status";

	if (type.equals("ApexTrigger"))
	    return "id, fullName, Body, apiVersion, status";

	if (type.equals("ApexPage"))
	    return "id, name, Markup, apiVersion";

	return "";
    }

    public static String getMeta(String type, double version, String status)
    {
	if (type.equals("ApexPage"))
	{
	    return "";
	}

	try
	{
	    String template = IOUtils.toString(new FileReader("xml/meta.xml"));

	    template = template.replace("{type}", type);
	    template = template.replace("{version}", version + "");
	    template = template.replace("{status}", status);

	    return template;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	return null;
    }

    public String getName()
    {
	return name;
    }

    public String getBody()
    {
	return body;
    }

    public double getVersion()
    {
	return version;
    }

    public String getStatus()
    {
	return status;
    }

}
