package com.github.autoforce.parser;

import apex.jorje.data.Loc.RealLoc;

public class ErrorObject
{
    private int line;
    private int column;
    private int startIndex;
    private int endIndex;
    private String message;

    public ErrorObject(RealLoc loc)
    {
	this.line = loc.line;
	this.column = loc.column;
	this.startIndex = loc.startIndex;
	this.endIndex = loc.endIndex;
    }

    public int getLine()
    {
	return line;
    }

    public int getColumn()
    {
	return column;
    }

    public int getStartIndex()
    {
	return startIndex;
    }

    public void setMessage(String message)
    {
	this.message = message;
    }

    public String getMessage()
    {
	return message;
    }

    public int getEndIndex()
    {
	return endIndex;
    }

    @Override
    public String toString()
    {
	return "ErrorObject [line=" + line + ", column=" + column + ", startIndex=" + startIndex + ", endIndex=" + endIndex + ", message=" + message + "]";
    }
}