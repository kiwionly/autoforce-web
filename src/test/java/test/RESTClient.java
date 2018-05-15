package test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class RESTClient 
{
	private String auth = "https://login.salesforce.com/services/oauth2/authorize";
	private String token = "https://test.salesforce.com/services/oauth2/token";
	
	private String accessToken;
	private String instanceUrl;
	
	public void auth() throws IOException
	{
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
		nvps.add(new BasicNameValuePair("client_id", "3MVG9Y6d_Btp4xp54pn2OAV9udZruP6K5DVr4mNVfHdTuw1Ya6Luz_xXCeh8clfn8z8Ce4ltZu80UpptnfE5S"));
		nvps.add(new BasicNameValuePair("client_secret", "2065564821565628640"));
		nvps.add(new BasicNameValuePair("redirect_uri", "http://localhost:8443/tool/success"));
		
		post(auth, nvps, new ResponseCallback() 
		{			
			@Override
			public void process(String response) {
				System.out.println(response);
			}
		});
	}
	
	public void restPOST(String url, List<NameValuePair> list, ResponseCallback callback) throws IOException
	{	
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpPost post = new HttpPost(instanceUrl + url);	
		post.addHeader(new BasicHeader("Authorization", "Bearer " + accessToken ));
		post.addHeader(new BasicHeader("Content-Type", "application/json" ));
		post.addHeader(new BasicHeader("X-PrettyPrint", "1" ));
						
		post.setEntity(new UrlEncodedFormEntity(list));
				
		CloseableHttpResponse response = client.execute(post);
		
		HttpEntity en = response.getEntity();
		
		InputStream in = en.getContent();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
			
		callback.process(out.toString());
		
		client.close();
	}

	public void token() throws IOException
	{
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("grant_type", "password"));
		nvps.add(new BasicNameValuePair("username", "gheewooi.ong@domain.com.test"));
		nvps.add(new BasicNameValuePair("password", "7Virtual_23coOvIOqe4jJPCRr6LLIZr2Wc"));
		nvps.add(new BasicNameValuePair("client_id", "3MVG9PerJEe9i8iJIQSkzmBv59uo6NYXwWMWEtfWoJ.h8Fo35t_3ng56UsCQNOoQZK2uSIGt61mW4xuotpjri"));
		nvps.add(new BasicNameValuePair("client_secret", "8266243347614319963"));
		
		post(token, nvps, new ResponseCallback() {
			
			@Override
			public void process(String response)
			{
				Map<String, String> map = JSON.parseObject(response, new TypeReference<Map<String, String>>() {});  
		
				String access = map.get("access_token");
				String instance = map.get("instance_url");
				
				accessToken = access;
				instanceUrl = instance;
			}
		});
		
	}
	
	public void post(String url, List<NameValuePair> list, ResponseCallback callback) throws IOException
	{
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpPost post = new HttpPost(url);	
				
		post.setEntity(new UrlEncodedFormEntity(list));
				
		CloseableHttpResponse response = client.execute(post);
		
		HttpEntity en = response.getEntity();
		
		InputStream in = en.getContent();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
	
		callback.process(out.toString());		
		
		client.close();
	}
	
	public void restGET(String url, List<NameValuePair> list, ResponseCallback callback) throws IOException, URISyntaxException
	{
		CloseableHttpClient client = HttpClients.createDefault();
		
		String parameters = "";
					 
		for (NameValuePair nameValuePair : list) 
		{
			String key = nameValuePair.getName();
			String value = nameValuePair.getValue();
			
			parameters = key + "=" + value;
		}
	        
		HttpGet get = new HttpGet("https://domain--test.cs30.my.salesforce.com" + url + parameters);
		get.addHeader(new BasicHeader("Authorization", "Bearer 00Dn0000000DFlb!ARoAQHKz8uk.CY8_yhH5qDBHdhoQA1yknUJJm_4.QXPz_nRuaXXQrdI9fu4l8AIZsXkfVZboLu3gir3m9bCASAjFDXx5p29u" ));
		get.addHeader(new BasicHeader("Content-Type", "application/json" ));
		get.addHeader(new BasicHeader("X-PrettyPrint", "1" ));
		
		long start = System.currentTimeMillis();
			
		CloseableHttpResponse response = client.execute(get);
		
		long stop = System.currentTimeMillis();
		
		System.out.println(stop - start);
		
		HttpEntity en = response.getEntity();
		
		InputStream in = en.getContent();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);

		callback.process(out.toString());		
		
		client.close();
	}
	
	public static interface ResponseCallback
	{
		public void process(String response);
	}
	
	
	public static void main(String[] args) throws Exception 
	{		
		RESTClient client = new RESTClient();
		client.token();
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//		nvps.add(new BasicNameValuePair("q", URLEncoder.encode("SELECT fullName FROM ApexClassMember", "UTF-8")));
//		nvps.add(new BasicNameValuePair("type", "SRS_JobTrk_PrintLabelExtension"));
		
		
		client.restGET("/services/data/v32.0/tooling/sobjects/ApexLog/07Ln0000005nk70EAA/Body/", nvps, new ResponseCallback() {
			
			@Override
			public void process(String response) 
			{
				System.out.println(response);
								
//				try {
//					
//					FileWriter writer = new FileWriter("class.txt");
//					writer.write(response);
//					writer.close();
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				
				
//				JSONObject all = JSON.parseObject(response);  
//				
//				JSONArray fields = all.getJSONArray("fields");
//				
//				Iterator<Object> iter = fields.iterator();
//								
//				while(iter.hasNext())
//				{
//					JSONObject node = (JSONObject)iter.next();
//					
//					String label = node.getString("label");
//					String name = node.getString("name");
//					
//					System.out.println(label + "," + name);
//				}
				
			}
		});
		
	}

}
