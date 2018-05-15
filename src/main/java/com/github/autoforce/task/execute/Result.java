package com.github.autoforce.task.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class Result
{
    private String column;
    private String compileProblem;
    private boolean compiled;
    private String exceptionMessage;
    private String exceptionStackTrace;
    private int line;
    private boolean success;
    private String debugLog;

    public String getColumn()
    {
	return column;
    }

    public void setColumn(String column)
    {
	this.column = column;
    }

    public String getCompileProblem()
    {
	return compileProblem;
    }

    public void setCompileProblem(String compileProblem)
    {
	this.compileProblem = compileProblem;
    }

    public boolean isCompiled()
    {
	return compiled;
    }

    public void setCompiled(boolean compiled)
    {
	this.compiled = compiled;
    }

    public String getExceptionMessage()
    {
	return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage)
    {
	this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionStackTrace()
    {
	return exceptionStackTrace;
    }

    public void setExceptionStackTrace(String exceptionStackTrace)
    {
	this.exceptionStackTrace = exceptionStackTrace;
    }

    public int getLine()
    {
	return line;
    }

    public void setLine(int line)
    {
	this.line = line;
    }

    public boolean isSuccess()
    {
	return success;
    }

    public void setSuccess(boolean success)
    {
	this.success = success;
    }

    public String getDebugLog()
    {
	return debugLog;
    }

    public void setDebugLog(String debugLog)
    {
	this.debugLog = debugLog;
    }

    @Override
    public String toString()
    {
	return "Result [column=" + column + ", compileProblem=" + compileProblem + ", compiled=" + compiled + ", exceptionMessage=" + exceptionMessage + ", exceptionStackTrace=" + exceptionStackTrace
		+ ", line=" + line + ", success=" + success + ", debugLog=" + debugLog + "]";
    }

    private String process(String response) throws IOException
    {
	BufferedReader buf = new BufferedReader(new StringReader(response));

	final StringBuilder builder = new StringBuilder();

	String line = null;

	while ((line = buf.readLine()) != null)
	{
	    if (line.contains("USER_DEBUG"))
	    {
		int index = line.lastIndexOf("|DEBUG|");

		if (index == -1)
		    continue;

		String out = line.substring(index + "|DEBUG|".length());

		builder.append(out.trim());
	    }
	}

	buf.close();

	return builder.toString();
    }

    public String getLog() throws IOException
    {
	return process(getDebugLog());
    }

}