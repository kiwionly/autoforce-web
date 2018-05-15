package com.github.autoforce.task.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class HttpClient
{
    public static String postSOAP(String url, String soap) throws IOException
    {
	CloseableHttpClient client = HttpClients.createDefault();

	HttpPost post = new HttpPost(url);
	post.addHeader(new BasicHeader("SOAPAction", "\"\""));
	post.addHeader(new BasicHeader("Content-Type", "text/xml"));

	post.setEntity(new StringEntity(soap, Charset.forName("UTF-8")));

	CloseableHttpResponse response = client.execute(post);

	HttpEntity en = response.getEntity();

	InputStream in = en.getContent();

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	IOUtils.copy(in, out);

	client.close();

	return out.toString();
    }

}
