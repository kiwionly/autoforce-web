package com.github.autoforce.scanner;

public class KeyPair
{
    private final String key;
    private final String value;

    public KeyPair(String key, String value)
    {
	this.key = key;
	this.value = value;
    }

    public String getKey()
    {
	return key;
    }

    public String getValue()
    {
	return value;
    }

    @Override
    public String toString()
    {
	return "KeyPair [key=" + key + ", value=" + value + "]";
    }

}