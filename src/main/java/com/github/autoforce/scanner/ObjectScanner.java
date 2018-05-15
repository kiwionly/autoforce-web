
package com.github.autoforce.scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.*;
import com.sforce.ws.ConnectionException;

public class ObjectScanner<T> extends ApexScanner<T>
{
    public ObjectScanner(LoginResult loginResult) throws ConnectionException
    {
	super(loginResult);
    }

    public void getReferenceObject(List<String> input) throws Exception
    { // where NamespacePrefix = null
	getReferenceClasses(input, "select Id, Name, Body from ApexClass where NamespacePrefix = null order by Name asc ", "referenceObject");
    }

    protected Map<String, Set<String>> getReferenceClasses(List<String> inputList, List<String> ids) throws ConnectionException
    {
	// Query again the ApexClassMember's to retrieve the SymbolTable's
	List<ApexClassMember> apexClassMembersWithSymbols = cast(conn.retrieve("FullName, Body, ContentEntityId, SymbolTable", "ApexClassMember", ids.toArray(new String[ids.size()])),
		ApexClassMember.class);

	Map<String, Set<String>> references = new HashMap<String, Set<String>>();

	for (String input : inputList)
	{
	    input = input.trim();

	    String column = null;

	    if (input.contains("."))
	    {
		int index = input.indexOf('.');
		column = input.substring(index + 1);

		input = input.substring(0, index);
	    }

	    Set<String> referencesClass = new HashSet<String>();

	    for (ApexClassMember apexClassMember : apexClassMembersWithSymbols)
	    {

		if (apexClassMember == null)
		    continue;

		// System.out.println("+---------------------------------");
		System.out.println("name = " + apexClassMember.getFullName());

		SymbolTable symbolTable = apexClassMember.getSymbolTable();

		if (symbolTable == null) // No symbol table, then class likely is invalid
		    continue;

		if (column == null)
		{
		    // need to loop variables and properties for instance and locatin variables
		    for (Symbol symbol : symbolTable.getVariables())
		    {
			String type = symbol.getType();

			if (type.equalsIgnoreCase(input))
			{
			    referencesClass.add(apexClassMember.getFullName());
			}
		    }

		    for (VisibilitySymbol properties : symbolTable.getProperties())
		    {
			String type = properties.getType();

			if (type.equalsIgnoreCase(input))
			{
			    referencesClass.add(apexClassMember.getFullName());
			}
		    }
		}

		// check if any object field exist
		if (column != null)
		{
		    Set<KeyPair> set = new HashSet<KeyPair>();

		    for (Symbol symbol : symbolTable.getVariables())
		    {
			String type = symbol.getType().toLowerCase();
			String name = symbol.getName().toLowerCase();

			set.add(new KeyPair(type, name));
		    }

		    for (VisibilitySymbol property : symbolTable.getProperties())
		    {
			String type = property.getType().toLowerCase();
			String name = property.getName().toLowerCase();

			set.add(new KeyPair(type, name));
		    }

		    String body = apexClassMember.getBody();

		    for (KeyPair keyPair : set)
		    {

			if (keyPair.getKey().equalsIgnoreCase(input))
			{
			    String variableName = keyPair.getValue();

			    StaticAnalyzer sa = new StaticAnalyzer(body, variableName + "." + column);

			    try
			    {
				List<Integer> result = sa.process();

				if (!result.isEmpty())
				    referencesClass.add(apexClassMember.getFullName());

			    }
			    catch (IOException e)
			    {
				e.printStackTrace();
			    }
			}
		    }
		}
	    }

	    // check if put a new one or append an existing one.
	    Set<String> exist = references.get(input);

	    if (exist == null)
	    {
		references.put(input, referencesClass);
	    }
	    else
	    {
		exist.addAll(referencesClass);
	    }
	}

	return references;
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception
    {
	// LoginResult loginResult = LoginUtil.loginToSalesforce("kiwionly@gmail.com",
	// "7Virtual_23TqhuSLUFsxX8aYL8jrNoy57v",
	// "https://login.salesforce.com/services/Soap/c/32");
	// LoginResult loginResult =
	// LoginUtil.loginToSalesforce("gheewooi.ong@.com.dev", "",
	// "https://test.salesforce.com/services/Soap/c/32");
	LoginResult loginResult = LoginUtil.loginToSalesforce("gheewooi.ong@.com.test", "", "https://test.salesforce.com/services/Soap/c/32");
	ObjectScanner<?> scanner = new ObjectScanner(loginResult);
	scanner.setUseWorkspace(true);
	List<String> queryClasses = new ArrayList<String>();
	// queryClasses.add("Account");
	queryClasses.add("Case");

	scanner.getReferenceObject(queryClasses);

	scanner.shutdown();
    }

}
