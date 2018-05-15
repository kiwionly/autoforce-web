package com.github.autoforce.task.coverage;

import java.util.HashSet;
import java.util.Set;

public class Records
{
    private String ApexClassName;

    private String ApexClassOrTriggerId;
    private String apexTestClassId;
    private Coverage coverage = new Coverage();
    private String TestMethodName;
    private int NumLinesUncovered;
    private int NumLinesCovered;

    public String getApexTestClassId()
    {
	return apexTestClassId;
    }

    public void setApexTestClassId(String apexTestClassId)
    {
	this.apexTestClassId = apexTestClassId;
    }

    public Coverage getCoverage()
    {
	return coverage;
    }

    public String getTestMethodName()
    {
	return TestMethodName;
    }

    public void setTestMethodName(String testMethodName)
    {
	TestMethodName = testMethodName;
    }

    public int getNumLinesUncovered()
    {
	return NumLinesUncovered;
    }

    public void setNumLinesUncovered(int numLinesUncovered)
    {
	NumLinesUncovered = numLinesUncovered;
    }

    public int getNumLinesCovered()
    {
	return NumLinesCovered;
    }

    public void setNumLinesCovered(int numLinesCovered)
    {
	NumLinesCovered = numLinesCovered;
    }

    @Override
    public String toString()
    {
	return "Records [apexTestClassId=" + apexTestClassId + ", coverage=" + coverage + ", TestMethodName=" + TestMethodName + ", NumLinesUncovered=" + NumLinesUncovered + ", NumLinesCovered="
		+ NumLinesCovered + "]";
    }

    public void addCoveredLines(int covered)
    {
	coverage.coveredLines.add(covered);
    }

    public void addUncoveredLines(int uncovered)
    {
	coverage.uncoveredLines.add(uncovered);
    }

    public double getCodeCoveragePercent()
    {
	double percent = ((double) coverage.coveredLines.size() / ((double) coverage.coveredLines.size() + (double) coverage.uncoveredLines.size())) * 100;

	return percent;
    }

    public String getApexClassOrTriggerId()
    {
	return ApexClassOrTriggerId;
    }

    public void setApexClassOrTriggerId(String apexClassOrTriggerId)
    {
	ApexClassOrTriggerId = apexClassOrTriggerId;
    }

    public String getApexClassName()
    {
	return ApexClassName;
    }

    public void setApexClassName(String apexClassName)
    {
	ApexClassName = apexClassName;
    }

    public class Coverage
    {
	private Set<Integer> coveredLines = new HashSet<Integer>();
	private Set<Integer> uncoveredLines = new HashSet<Integer>();

	public Set<Integer> getCoveredLines()
	{
	    return coveredLines;
	}

	public Set<Integer> getUncoveredLines()
	{
	    return uncoveredLines;
	}

	@Override
	public String toString()
	{
	    return "Coverage [coveredLines=" + coveredLines + ", uncoveredLines=" + uncoveredLines + "]";
	}

    }

}
