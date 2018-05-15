package test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class ToolingAPIDeployment
{

    public static void main(String[] args) throws Exception
    {
	LoginResult login = login();
	ToolingConnection tooling = LoginUtil.createToolingConnection(login);

	long start = System.currentTimeMillis();

	execute(tooling);

	long end = System.currentTimeMillis();

	System.out.println(end - start);

    }

    private static void execute(ToolingConnection tooling) throws ConnectionException, IOException, InterruptedException, ExecutionException
    {

    }

    public static LoginResult login() throws ConnectionException
    {
	String USERNAME = "";
	String PASSWORD = "";
	String URL = "https://test.salesforce.com/services/Soap/c/32.0";
	LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return loginResult;
    }
}
