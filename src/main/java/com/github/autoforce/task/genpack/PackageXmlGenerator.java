package com.github.autoforce.task.genpack;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.sforce.soap.metadata.FileProperties;

public class PackageXmlGenerator
{
    public PackageXmlGenerator()
    {
    }

    public String generate(Set<String> types, List<FileProperties> files, double version)
    {
	sort(files);

	StringBuilder buf = new StringBuilder();

	buf.append(header());

	for (String type : types)
	{
	    String startType = "\t<types>";
	    buf.append(startType);
	    buf.append("\n");

	    for (FileProperties fp : files)
	    {

		if (fp.getType().equals(type))
		{
		    String member = "\t\t<members>" + fp.getFullName().trim() + "</members>";

		    buf.append(member);
		    buf.append("\n");
		}
	    }

	    String name = "\t\t<name>" + type + "</name>";
	    buf.append(name);
	    buf.append("\n");

	    String endType = "\t</types>";
	    buf.append(endType);
	    buf.append("\n");
	}

	buf.append(version(version));
	buf.append(footer());

	return buf.toString();
    }

    private void sort(List<FileProperties> files)
    {
	Collections.sort(files, new Comparator<FileProperties>() {

	    @Override
	    public int compare(FileProperties o1, FileProperties o2)
	    {
		return o1.getFullName().compareTo(o2.getFullName());
	    }
	});
    }

    public void generateFile(String file, String content) throws IOException
    {
	FileWriter writer = new FileWriter(file);

	writer.write(content);

	writer.close();
    }

    private String header()
    {
	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\n";
    }

    private String version(double version)
    {
	return "\t<version>" + version + "</version>\n";
    }

    private String footer()
    {
	return "</Package>";
    }

}
