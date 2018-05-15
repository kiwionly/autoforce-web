package com.github.autoforce.task.genpack.readFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class StaticAnalyzer
{
    private int total = 0;
    private List<FileSet> list = new ArrayList<FileSet>();

    public StaticAnalyzer() throws IOException
    {
	Iterator<File> iter = FileUtils.iterateFiles(new File("load/src"), new String[] { "cls", "trigger" }, true);

	while (iter.hasNext())
	{
	    File file = iter.next();

	    process(file);
	}

	// sort the array
	Collections.sort(list);

	for (FileSet file : list)
	{
	    System.out.println(file.getName() + "  - " + file.getCount());
	}

	System.out.println("total =" + total);
    }

    private void process(File file) throws IOException
    {
	BufferedReader buf = new BufferedReader(new FileReader(file));

	String line = "";
	int count = 0;

	while ((line = buf.readLine()) != null)
	{
	    if (check(line))
	    {
		count += 1;
	    }
	}

	buf.close();

	total += count;

	list.add(new FileSet(file.getName(), count));

    }

    private boolean check(String line)
    {
	String newLine = line.toLowerCase();
	newLine = newLine.trim();

	if (newLine.startsWith("/*"))
	    return false;

	if (newLine.startsWith("*"))
	    return false;

	if (newLine.startsWith("//"))
	    return false;

	if (newLine.contains("system.debug("))
	    return true;

	return false;
    }

    private class FileSet implements Comparable<FileSet>
    {
	private final String name;
	private final int count;

	public FileSet(String name, int count)
	{
	    this.name = name;
	    this.count = count;
	}

	public String getName()
	{
	    return name;
	}

	public int getCount()
	{
	    return count;
	}

	@Override
	public int compareTo(FileSet fs)
	{
	    if (fs.count < count)
		return -1;

	    if (fs.count > count)
		return 1;

	    return 0;
	}

	@Override
	public String toString()
	{
	    return "FileSet [name=" + name + ", count=" + count + "]";
	}
    }

    public static void main(String[] args) throws IOException
    {
	new StaticAnalyzer();
    }
}
