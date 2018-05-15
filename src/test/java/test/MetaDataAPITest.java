package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.autoforce.LoginUtil;
import com.sforce.soap.enterprise.DescribeSObjectResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Field;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.sobject.SObject;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

public class MetaDataAPITest {

	public static void main(String[] args)  throws ConnectionException 
	{		
		LoginResult login = login();		
		MetadataConnection soap = LoginUtil.createMetadataConnection(login);
		
		long start = System.currentTimeMillis();
		
		ListMetadataQuery[] x  = new ListMetadataQuery[1];
		
		ListMetadataQuery query = new ListMetadataQuery();
		query.setType("ApexClass");
		
		x[0] = query;
		
		FileProperties[] file = soap.listMetadata(x , 32.0);
		
		for (FileProperties fileProperties : file) {
			System.out.println(fileProperties.getFileName());
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println(end - start);
			
	}

	public static List<String> getStandardObjectName()
    {
    	List<String> list = new ArrayList<String>();
    	
    	Reflections reflections = new Reflections("com.sforce.soap.enterprise.sobject");
		Set<Class<? extends SObject>> subTypes = reflections.getSubTypesOf(SObject.class);
			
		//sort the name
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
			
			if(!(name.endsWith("__Tag") || name.endsWith("__History")  || name.endsWith("__c") || name.endsWith("__Share") ))
			{
				list.add(name);
			}
			
		}
		
		return list;
    }


	private static void getCustomObjectProperties(EnterpriseConnection soap) throws ConnectionException
	{
		DescribeSObjectResult cases = soap.describeSObject("SRS_CC_Error__c");
		
		Field[] x = cases.getFields();
		
		for (Field field : x) {
			System.out.println(field.getName());
		}
	}



	
	public static LoginResult login() throws ConnectionException 
    {
        String USERNAME = "gheewooi.ong@domain.com.test";
        String PASSWORD = ""; 
        String URL = "https://test.salesforce.com/services/Soap/c/32.0";
        LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);
       
        return loginResult;
    }
}
