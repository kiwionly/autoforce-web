package com.github.autoforce.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

//@WebFilter(asyncSupported=true, value="/login.do")
public class LoginFilter implements Filter
{
    CloseableHttpClient client;

    @Override
    public void destroy()
    {
	client = null;
    }

    private CloseableHttpClient createHttpConnection(HttpRequestBase request)
    {
	RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).build();

	request.setConfig(requestConfig);

	return client;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
	URI uri = null;

	try
	{
	    uri = new URIBuilder().setScheme("https").setHost("google.com").build();
	}
	catch (URISyntaxException e)
	{
	    e.printStackTrace();
	}

	HttpGet httpget = new HttpGet(uri);

	httpget.addHeader("Accept-Encoding", "gzip");

	CloseableHttpClient httpClient = createHttpConnection(httpget);
	HttpResponse response = httpClient.execute(httpget);
	HttpEntity entity = response.getEntity();

	if (entity != null)
	{

	    InputStream in = entity.getContent();

	    Header contentEncoding = response.getFirstHeader("Content-Encoding");

	    if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
	    {
		in = new GZIPInputStream(in);
	    }

	    BufferedReader buf = new BufferedReader(new InputStreamReader(in));
	    String line = buf.readLine();
	    System.out.println(line);
	    buf.close();
	}

	chain.doFilter(req, res);

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
	client = HttpClients.createDefault();
    }

}
