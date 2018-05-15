package com.github.autoforce.task.execute;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.codehaus.stax2.XMLInputFactory2;

import com.github.autoforce.task.util.HttpClient;

/*
 * SalesForce return result in SOAP header, and the java generate class no method to read the header value,
 * 
 * so, you need to parse you own.
 * 
 * 
 */
public class ExecuteAnonymousProcessor
{
    public Result query(String url, String session, File file, String category, String level, String code) throws IOException
    {
	String request = IOUtils.toString(new FileInputStream(file));

	request = request.replace("{session}", session);
	request = request.replace("{category}", category);
	request = request.replace("{level}", level);
	request = request.replace("{code}", code);

	String response = HttpClient.postSOAP(url, request);

	return parse(new ByteArrayInputStream(response.getBytes()));
    }

    public Result parse(InputStream input)
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

	Result result = null;

	try
	{
	    XMLStreamReader reader = xmlif.createXMLStreamReader(input);

	    while (reader.hasNext())
	    {
		if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
		{
		    String name = reader.getLocalName();

		    if (name.equals("DebuggingInfo"))
		    {
			result = new Result();
		    }

		    if (name.equals("debugLog"))
		    {
			String val = getElementText(reader);

			result.setDebugLog(val);
		    }

		    if (name.equals("column"))
		    {
			String val = getElementText(reader);

			result.setColumn(val);
		    }

		    if (name.equals("compileProblem"))
		    {
			String val = getElementText(reader);

			result.setCompileProblem(val);
		    }

		    if (name.equals("compiled"))
		    {
			String val = getElementText(reader);

			boolean compiled = Boolean.parseBoolean(val);

			result.setCompiled(compiled);
		    }

		    if (name.equals("exceptionMessage"))
		    {
			String val = getElementText(reader);

			result.setExceptionMessage(val);
		    }

		    if (name.equals("exceptionStackTrace"))
		    {
			String val = getElementText(reader);

			result.setExceptionStackTrace(val);

		    }

		    if (name.equals("line"))
		    {
			String val = getElementText(reader);

			int line = Integer.parseInt(val);

			result.setLine(line);
		    }

		    if (name.equals("success"))
		    {
			String val = getElementText(reader);

			boolean success = Boolean.parseBoolean(val);

			result.setSuccess(success);

		    }
		}

		reader.next();
	    }

	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	return result;
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

}
