package test;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.DescribeGlobalResult;
import com.sforce.soap.enterprise.DescribeGlobalSObjectResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class ApexCodeCoverageReader
{

    public static ToolingConnection login() throws ConnectionException
    {
	final String USERNAME = "";
	final String PASSWORD = "";
	final String URL = "";

	final LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return LoginUtil.createToolingConnection(loginResult);
    }

    public static void main(String[] args) throws ConnectionException
    {

	ToolingConnection conn = login();

	String classBody = "public class SRS_Messages {\n" + "public string SayHello() {\n" + " return 'Hello lalalala ';\n" + "}\n" + "}";

	// create a new ApexClass object and set the body
	// ApexClass apexClass = new ApexClass();
	// apexClass.setBody(classBody);
	// apexClass.setId("01pn00000004bryAAA");
	//
	// long start = System.currentTimeMillis();
	//
	// ApexClass[] classes = { apexClass };
	// // call create() to add the class
	// SaveResult[] saveResults = conn.update(classes);
	//
	// long end = System.currentTimeMillis();
	// System.out.println(end - start);
	//
	// for (int i = 0; i < saveResults.length; i++) {
	// if (saveResults[i].isSuccess()) {
	// System.out.println("Successfully created Class: " + saveResults[i].getId());
	// } else {
	// System.out.println("Error: could not create Class ");
	// System.out.println("The error reported was: "+
	// saveResults[i].getErrors()[0].getMessage() + "\n");
	// }
	// }

	// List<String> ids = new ArrayList<String>();
	//
	// String soql = "SELECT id, Body, name, ApiVersion, Status FROM ApexClass where
	// name like 'SRS%' ";
	// QueryResult result = conn.query(soql);
	//
	// for (SObject object : result.getRecords()) {
	//
	// ApexClass clazz = (ApexClass)object;
	//
	// String id = clazz.getId();
	// String name = clazz.getName();
	// String st = clazz.getBody();
	//
	// if(name.contains("Test"))
	// System.out.println(name);
	// }
	// System.out.println(ids);
	// String sql = "SELECT ApexTestClassId, TestMethodName, NumLinesCovered,
	// NumLinesUncovered, Coverage FROM ApexCodeCoverage WHERE ApexClassOrTriggerId
	// = '01p30000001LGcl' ";
	//
	// QueryResult result = conn.query(sql);
	//
	// System.out.println(result.getSize());
	//
	// for (SObject object : result.getRecords()) {
	//
	// ApexCodeCoverage acc = (ApexCodeCoverage)object;
	// Coverage coverage = acc.getCoverage();
	//
	// int[] covered = coverage.getCoveredLines();
	// int[] uncovered = coverage.getUncoveredLines();
	//
	// Set<Integer> cover = new HashSet<Integer>();
	// for (Integer co : covered) {
	// cover.add(co);
	// }
	//
	// Set<Integer> uncover = new HashSet<Integer>();
	// for (Integer un : uncovered) {
	// uncover.add(un);
	// }
	//
	// double percent = (cover.size() / (cover.size() + uncover.size())) * 100;
	//
	// System.out.println("Total class coverage is " + percent + "%.");
	// }
	//

	QueryResult result = conn.query(
		"SELECT ApexTestClassId, ApexClassorTriggerId, TestMethodName, NumLinesCovered, NumLinesUncovered FROM ApexCodeCoverage WHERE ApexClassOrTriggerId in ('01p30000001LGcl', '01p30000001LGcn')");

	System.out.println(result.getSize());

	for (SObject object : result.getRecords())
	{

	    ApexCodeCoverage acc = (ApexCodeCoverage) object;

	    double covered = acc.getNumLinesCovered();
	    double uncovered = acc.getNumLinesUncovered();

	    double percent = (covered / (covered + uncovered)) * 100;

	    System.out.println(acc.getApexClassOrTriggerId() + " -> " + acc.getApexTestClassId() + " -> " + acc.getTestMethodName() + " covered " + percent + "%.");
	}

	// describeGlobalSample(conn);

    }

    public static void describeGlobalSample(EnterpriseConnection connection)
    {
	try
	{
	    // Make the describeGlobal() call
	    DescribeGlobalResult describeGlobalResult = connection.describeGlobal();

	    // Get the sObjects from the describe global result
	    DescribeGlobalSObjectResult[] sobjectResults = describeGlobalResult.getSobjects();

	    // Write the name of each sObject to the console
	    for (int i = 0; i < sobjectResults.length; i++)
	    {
		System.out.println(sobjectResults[i].getName());

	    }
	}
	catch (ConnectionException ce)
	{
	    ce.printStackTrace();
	}
    }

}
