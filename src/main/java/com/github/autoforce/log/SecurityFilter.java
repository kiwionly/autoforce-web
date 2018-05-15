package com.github.autoforce.log;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(value = "/log.do")
public class SecurityFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
	// do nothing
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException
    {
	String ipAddress = req.getRemoteHost();
	System.out.println(ipAddress);
	// if(ipAddress.endsWith(".salesforce.com"))
	// {
	// return;
	// }

	chain.doFilter(req, res);
    }

    @Override
    public void destroy()
    {
	// do nothing
    }
}