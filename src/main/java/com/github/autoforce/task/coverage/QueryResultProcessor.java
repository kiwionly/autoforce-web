package com.github.autoforce.task.coverage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLInputFactory2;

import com.github.autoforce.task.util.HttpClient;

/*
 * salesforce lib got type mapper error for Coverage generated class, 
 * this class will parse and get the correct result, remove this after Salesforce fixed the type mapper issue. , recent release api 33 still got issue !
 * 
 */
public class QueryResultProcessor
{
    public List<Records> query(String url, String session, String query, String file) throws IOException
    {
	String request = IOUtils.toString(new FileInputStream(file));

	request = request.replace("{session}", session);
	request = request.replace("{query}", query);

	String response = HttpClient.postSOAP(url, request);

	return parse(new ByteArrayInputStream(response.getBytes()));
    }

    public List<Records> parse(InputStream input)
    {
	XMLInputFactory2 xmlif = null;

	try
	{
	    xmlif = (XMLInputFactory2) XMLInputFactory.newInstance();
	    xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
	    xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
	    xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
	    xmlif.configureForSpeed();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	List<Records> recordList = new ArrayList<Records>();

	long starttime = System.currentTimeMillis();

	try
	{
	    XMLStreamReader reader = xmlif.createXMLStreamReader(input);

	    Records records = null;

	    while (reader.hasNext())
	    {
		if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
		{
		    String name = reader.getLocalName();

		    if (name.equals("records"))
		    {
			records = new Records();
		    }

		    if (name.equals("ApexClassOrTriggerId"))
		    {
			String val = getElementText(reader);

			records.setApexClassOrTriggerId(val);
		    }

		    if (name.equals("ApexTestClassId"))
		    {
			String val = getElementText(reader);

			records.setApexTestClassId(val);
		    }

		    if (name.equals("NumLinesCovered"))
		    {
			String val = getElementText(reader);

			int covered = Integer.parseInt(val);

			records.setNumLinesCovered(covered);
		    }

		    if (name.equals("NumLinesUncovered"))
		    {
			String val = getElementText(reader);

			int covered = Integer.parseInt(val);

			records.setNumLinesUncovered(covered);
		    }

		    if (name.equals("coveredLines"))
		    {
			String val = getElementText(reader);

			int covered = Integer.parseInt(val);

			records.addCoveredLines(covered);
		    }

		    if (name.equals("uncoveredLines"))
		    {
			String val = getElementText(reader);

			int uncovered = Integer.parseInt(val);

			records.addUncoveredLines(uncovered);

		    }

		    if (name.equals("TestMethodName"))
		    {
			String val = getElementText(reader);
			records.setTestMethodName(val);
		    }

		}

		if (reader.getEventType() == XMLStreamConstants.END_ELEMENT)
		{
		    String name = reader.getLocalName();

		    if (name.equals("records"))
		    {
			recordList.add(records);

			records = null;
		    }
		}

		reader.next();
	    }

	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	System.out.println(" completed in " + (System.currentTimeMillis() - starttime) + " ms");

	return recordList;
    }

    private String getElementText(XMLStreamReader reader) throws XMLStreamException
    {
	String text = "";

	while (reader.hasNext())
	{
	    if (reader.getEventType() == XMLStreamConstants.END_ELEMENT)
	    {
		break;
	    }
	    else
		if (reader.getEventType() == XMLStreamConstants.CHARACTERS)
		{
		    text += reader.getText();
		}

	    reader.next();
	}

	return text;
    }

    public static void main(String[] args) throws Exception
    {
	QueryResultProcessor test = new QueryResultProcessor();

	test.query("https://domain--Test.cs30.my.salesforce.com/services/Soap/T/32.0",
		"00Dn0000000DFlb!ARoAQNUV9XbLxf1VEGbiMRbPjeAwasg6Te6F9G8smD5RcPENOVFbfov2NFArNfh79fCJfDUYMTzZ5dW3_p2a39giYzmFubp.",
		"SELECT ApexTestClassId, ApexClassorTriggerId, TestMethodName, NumLinesCovered, NumLinesUncovered, Coverage FROM ApexCodeCoverage WHERE ApexClassOrTriggerId in ('01p30000001LGcl', '01p30000001LGcn') ",
		"xml/request.xml");

    }

}
