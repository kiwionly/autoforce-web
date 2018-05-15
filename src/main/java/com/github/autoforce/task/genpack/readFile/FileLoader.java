package com.github.autoforce.task.genpack.readFile;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;

import com.github.autoforce.LoginUtil;
import com.github.autoforce.task.genpack.MetadataReader;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

public class FileLoader
{
    private final List<FileProperties> filterList;
    private ExecutorService executor;
    private String filePath;

    public FileLoader(List<FileProperties> filterList, String filePath)
    {
	executor = Executors.newCachedThreadPool();

	this.filterList = filterList;
	this.filePath = filePath;
    }

    private void load(ToolingConnection conn, Set<String> types) throws Exception
    {
	List<Future<List<File>>> futureList = new ArrayList<Future<List<File>>>();

	for (String type : types)
	{
	    List<FileProperties> fps = new ArrayList<FileProperties>();

	    for (FileProperties fp : filterList)
	    {

		if (fp.getType().equals(type))
		{
		    fps.add(fp);
		}
	    }
	    LoaderTask apexLoader = new LoaderTask(conn, fps, type);

	    Future<List<File>> future = executor.submit(apexLoader);

	    futureList.add(future);
	}

	for (Future<List<File>> future : futureList)
	{

	    List<File> result = future.get();

	    for (File file : result)
	    {
		System.out.println(file);
	    }
	}
    }

    private String createParameter(List<FileProperties> fileProperties)
    {
	StringBuilder buf = new StringBuilder();

	buf.append("(");

	for (FileProperties fp : fileProperties)
	{
	    buf.append("'");
	    buf.append(fp.getId());
	    buf.append("',");
	}

	String all = buf.toString();
	all = all.substring(0, all.length() - 1);

	return all + ")";
    }

    public class LoaderTask implements Callable<List<File>>
    {
	private ToolingConnection conn;
	private List<FileProperties> fps;
	private String type;

	public LoaderTask(ToolingConnection conn, List<FileProperties> fps, String type)
	{
	    this.conn = conn;
	    this.type = type;
	    this.fps = fps;
	}

	@Override
	public List<File> call() throws Exception
	{
	    List<File> fileList = new ArrayList<File>();

	    String name = createParameter(fps);

	    String soql = "select " + ApexType.getColumn(type) + " from " + type + " where id in " + name;

	    QueryResult result = conn.query(soql);

	    for (SObject obj : result.getRecords())
	    {

		ApexType apex = new ApexType(obj);

		String fullName = apex.getName();
		String body = apex.getBody();
		double version = apex.getVersion();
		String status = apex.getStatus();

		for (FileProperties fp : fps)
		{
		    if (fp.getFullName().equals(fullName))
		    {
			// write file
			File file = new File(filePath + fp.getFileName());

			String dir = getDir(file.getPath());
			FileUtils.forceMkdir(new File(dir));

			FileWriter writer = new FileWriter(file);
			writer.write(body);
			writer.close();

			// write meta file
			// if(!type.equals("ApexPage"))
			// {
			// FileWriter meta = new FileWriter(file + "-meta.xml");
			// meta.write(createMetaData(type, version, status));
			// meta.close();
			// }

			fileList.add(file);
		    }
		}
	    }

	    return fileList;
	}

    }

    private String createMetaData(String type, double version, String status)
    {
	String meta = ApexType.getMeta(type, version, status);

	return meta;
    }

    private String getDir(String path)
    {

	int index = path.lastIndexOf(File.separator);
	String dir = path.substring(0, index);

	return dir;
    }

    public void shutdown()
    {
	executor.shutdown();
    }

    public static LoginResult login() throws ConnectionException
    {
	String USERNAME = "kiwionly@gmail.com";
	String PASSWORD = "7VirtualV7qxCRtKzMx4aYaaPR2IziQA3";
	String URL = "https://login.salesforce.com/services/Soap/c/42.0";
	LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);

	return loginResult;
    }

    public static void main(String[] args) throws Exception
    {
	long start = System.currentTimeMillis();

	Set<String> metaTypes = new HashSet<String>();
	// metaTypes.add("ApexPage");
	metaTypes.add("ApexTrigger");
	// metaTypes.add("Translations");
	// metaTypes.add("PermissionSet");
	metaTypes.add("ApexClass");

	Set<String> excludes = new HashSet<String>();
	excludes.add("Translations");

	LoginResult login = login();

	MetadataConnection conn = LoginUtil.createMetadataConnection(login);
	MetadataReader metadataReader = new MetadataReader();

	List<FileProperties> fp = metadataReader.getFileList(conn, metaTypes, 31.0);

	Set<String> preFix = new HashSet<String>();
	preFix.add("SRS");

	List<FileProperties> filterList = metadataReader.filter(preFix, fp, excludes);

	FileLoader loader = new FileLoader(filterList, "load/src/");

	ToolingConnection tooling = LoginUtil.createToolingConnection(login);

	Set<String> types = new HashSet<String>();
	types.add("ApexClass");
	types.add("ApexTrigger");
	// types.add("ApexPage");

	loader.load(tooling, types);

	long end = System.currentTimeMillis();

	System.out.println(end - start);

	loader.shutdown();

    }

}
