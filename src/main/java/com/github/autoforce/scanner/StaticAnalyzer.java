package com.github.autoforce.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StaticAnalyzer
{
    private final String content;
    private final String input;

    public StaticAnalyzer(String content, String input)
    {
	this.content = content;
	this.input = input;
    }

    public List<Integer> process() throws IOException
    {
	List<Integer> list = new ArrayList<Integer>();

	BufferedReader buf = new BufferedReader(new StringReader(content));

	String line = "";
	int count = 0;

	while ((line = buf.readLine()) != null)
	{
	    if (check(line, input))
	    {
		count += 1;

		list.add(count);
	    }
	}

	buf.close();

	return list;

    }

    private boolean check(String line, String input)
    {
	String newLine = line.toLowerCase();
	newLine = newLine.trim();

	if (newLine.startsWith("/*"))
	    return false;

	if (newLine.startsWith("*"))
	    return false;

	if (newLine.startsWith("//"))
	    return false;

	if (newLine.contains(input.toLowerCase()))
	    return true;

	return false;
    }

    public static void main(String[] args) throws IOException
    {
	String content = "public class Test { \n case.field; \n }";

	StaticAnalyzer sa = new StaticAnalyzer(content, "cAse.field");

	List<Integer> result = sa.process();

	System.out.println(result);
    }

}
