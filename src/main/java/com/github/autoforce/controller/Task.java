package com.github.autoforce.controller;

public class Task
{
    private final String url;
    private final String name;
    private final String desc;

    public Task(String url, String name, String desc)
    {
	this.url = url;
	this.name = name;
	this.desc = desc;
    }

    public String getUrl()
    {
	return url;
    }

    public String getName()
    {
	return name;
    }

    public String getDesc()
    {
	return desc;
    }

    @Override
    public String toString()
    {
	return "Task [url=" + url + ", name=" + name + ", desc=" + desc + "]";
    }

}