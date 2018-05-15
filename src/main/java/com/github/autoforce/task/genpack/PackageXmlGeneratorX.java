package com.github.autoforce.task.genpack;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sforce.soap.metadata.FileProperties;

public class PackageXmlGeneratorX
{
    public PackageXmlGeneratorX()
    {
    }

    public String generate(Set<String> types, List<String> files, double version)
    {
	sort(files);

	StringBuilder buf = new StringBuilder();

	buf.append(header());

	for (String type : types)
	{
	    String startType = "\t<types>";
	    buf.append(startType);
	    buf.append("\n");

	    for (String fp : files)
	    {

		String member = "\t\t<members>" + fp + "</members>";

		buf.append(member);
		buf.append("\n");

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

    private void sort(List<String> files)
    {
	Collections.sort(files, new Comparator<String>() {

	    @Override
	    public int compare(String o1, String o2)
	    {
		return o1.compareTo(o2);
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

    public static void main(String[] args)
    {

	String data = "CSO_SLA, chatter_answers_question_escalation_to_case_trigger, CSS_X_CaseCommentTrigger, PLM_SalesSupport_SendEmail_Trg, LFSOverrideCountTrigger, CSO_ServiceConsole_CaseCommentValidation, CSO_ServiceConsole_EmailPolicyEnforcement, SRS_JobTrk_CaseClosed_Trg, CSO_ServiceConsole_Before, CSO_ServiceConsole_IncEmailReoCase, SRS_Jobtrk_DDupdateccTrans, SRS_JobTrack_EmailLanguage, zymeCaseSubjectAssignment, PLM_SalesSupport_Trg, CSO_Survey_After, SRS_JobTrack_Opportunity, CPR_CreateTaskForEDI_Trg, SRS_JobTrk_Lead_Case_Assignment, SRS_Jobtrk_AutoCreate_Quote_Invoice_trg, SRS_Account_Maintenance, CSS_X_CaseTrigger, PLM_UpdateFiscalQuarter_TRG, citAccountNameLookup, SRS_CaseTrigger, CSS_X_SynchronizeWithJIRAIssue, CaseOwnerTypeTrigger, SRS_JobTrk_Case_field_Update, CSO_ServiceConsole_RelatedCaseCopy, SRS_Close_Fulcrum_Claim";
	List<String> list = new ArrayList<String>();

	String[] classes = data.split(", ");

	for (String c : classes)
	{
	    list.add(c);
	}
	System.out.println(list.size());
	Set<String> set = new HashSet<>();
	set.add("ApexTrigger");

	String x = new PackageXmlGeneratorX().generate(set, list, 3.2);

	System.out.println(x);

    }

}
