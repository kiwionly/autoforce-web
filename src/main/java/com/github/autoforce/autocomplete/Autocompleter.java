package com.github.autoforce.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.DescribeSObjectResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Field;
import com.sforce.soap.enterprise.FieldType;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.tooling.sobject.ApexClass;
import com.sforce.soap.tooling.Method;
import com.sforce.soap.tooling.Parameter;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SymbolTable;
import com.sforce.soap.tooling.SymbolVisibility;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.soap.tooling.VisibilitySymbol;
import com.sforce.ws.ConnectionException;

public class Autocompleter
{
    private ToolingConnection tooling;
    private EnterpriseConnection enterprise;

    private Set<String> cached;

    public Autocompleter(ToolingConnection tooling, EnterpriseConnection enterprise)
    {
	cached = getStandardObjectName();

	this.tooling = tooling;
	this.enterprise = enterprise;
    }

    public List<String> getSuggestText(String classORObjectName) throws ConnectionException
    {
	if (classORObjectName == null)
	    throw new NullPointerException("class OR Object Name cannot be null");

	if (classORObjectName.endsWith("__c"))
	{
	    // custom object
	    return getSObject(classORObjectName);
	}
	else
	    if (isStandardObject(classORObjectName))
	    {
		// standard Object
		return getSObject(classORObjectName);
	    }
	    else
		if (classORObjectName.contains("_"))
		{
		    // apex class
		    return getApexClass(classORObjectName);
		}
		else
		{
		    // system class

		}

	return new ArrayList<String>();
    }

    private boolean isStandardObject(String classORObjectName)
    {
	if (cached.contains(classORObjectName))
	    return true;

	return false;
    }

    private Set<String> getStandardObjectName()
    {
	Set<String> set = new HashSet<String>();

	Reflections reflections = new Reflections("com.sforce.soap.enterprise.sobject");
	Set<Class<? extends SObject>> subTypes = reflections.getSubTypesOf(SObject.class);

	// sort the name
	List<Class<? extends SObject>> myList = new ArrayList<Class<? extends SObject>>(subTypes);
	Collections.sort(myList, new Comparator<Class<? extends SObject>>() {

	    @Override
	    public int compare(Class<? extends SObject> o1, Class<? extends SObject> o2)
	    {
		return o1.getSimpleName().compareTo(o2.getSimpleName());
	    }
	});

	for (Class<? extends SObject> class1 : myList)
	{
	    String name = class1.getSimpleName();

	    if (!(name.endsWith("__Tag") || name.endsWith("__History") || name.endsWith("__c") || name.endsWith("__Share")))
	    {
		set.add(name);
	    }
	}

	return set;
    }

    private List<String> getSObject(String classORObjectName) throws ConnectionException
    {
	List<String> list = new ArrayList<String>();

	DescribeSObjectResult cases = enterprise.describeSObject(classORObjectName);

	Field[] fields = cases.getFields();

	for (Field field : fields)
	{
	    String name = field.getName();
	    FieldType type = field.getType();

	    // System.out.println(name + " - " + type);

	    list.add(name);
	}

	return list;
    }

    private List<String> getApexClass(String classORObjectName) throws ConnectionException
    {
	List<String> list = new ArrayList<String>();

	QueryResult result = tooling.query("select name, SymbolTable from ApexClass where name = '" + classORObjectName + "' ");

	for (com.sforce.soap.tooling.sobject.SObject obj : result.getRecords())
	{
	    ApexClass apex = (ApexClass) obj;

	    SymbolTable table = apex.getSymbolTable();

	    getMethods(list, table);

	    getProperties(list, table);
	}

	return list;
    }

    private void getProperties(List<String> list, SymbolTable table)
    {
	VisibilitySymbol[] proList = table.getProperties();

	for (VisibilitySymbol pro : proList)
	{
	    String name = pro.getName();

	    String[] visiblilty = pro.getModifiers();

	    for (String mod : visiblilty)
	    {
		if (mod.equals("public"))
		{
		    StringBuilder buf = new StringBuilder();

		    buf.append(name);

		    list.add(buf.toString());
		}
	    }
	}
    }

    private void getMethods(List<String> list, SymbolTable table)
    {
	Method[] m = table.getMethods();

	for (Method method : m)
	{
	    String name = method.getName();
	    Parameter[] parameters = method.getParameters();

	    String[] visiblilty = method.getModifiers();

	    for (String mod : visiblilty)
	    {
		if (mod.equals("public"))
		{
		    StringBuilder buf = new StringBuilder();

		    buf.append(name);
		    buf.append("(");

		    for (int i = 0; i < parameters.length; i++)
		    {

			buf.append(parameters[i].getName());

			if (i < parameters.length - 1)
			    buf.append(",");
		    }

		    buf.append(")");

		    list.add(buf.toString());
		}
	    }
	}
    }

    public static void main(String[] args) throws ConnectionException
    {
	LoginResult login = login();
	ToolingConnection tooling = LoginUtil.createToolingConnection(login);
	EnterpriseConnection enterprise = LoginUtil.createConnection(login);

	Autocompleter auto = new Autocompleter(tooling, enterprise);

	System.out.println("ready : ");

	long start = System.currentTimeMillis();

	List<String> list = auto.getSuggestText("Case");

	long end = System.currentTimeMillis();

	System.out.println(list.size());

	List<String> myList = new ArrayList<String>(list);
	Collections.sort(myList, new Comparator<String>() {

	    @Override
	    public int compare(String o1, String o2)
	    {
		return o1.compareTo(o2);
	    }
	});

	for (String token : myList)
	{
	    System.out.println(token);
	}

	System.out.println(end - start);
    }

    public static LoginResult login() throws ConnectionException
    {
	String USERNAME = "";
	String PASSWORD = "";
	String URL = "";
	LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return loginResult;
    }

}
