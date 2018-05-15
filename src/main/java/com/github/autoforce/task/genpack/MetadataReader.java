package com.github.autoforce.task.genpack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.reflections.Reflections;

import com.github.autoforce.LoginUtil;
import com.google.common.collect.Lists;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

public class MetadataReader
{
    private static final int MAX_BATCH_SIZE = 3;
    private ExecutorService executor;

    public MetadataReader()
    {
	executor = Executors.newCachedThreadPool();
    }

    public List<FileProperties> getFileList(MetadataConnection conn, Set<String> metaTypes, double version)
    {
	List<Future<List<FileProperties>>> futures = new ArrayList<Future<List<FileProperties>>>();

	List<FileProperties> all = new ArrayList<FileProperties>();
	List<ListMetadataQuery> queries = new ArrayList<ListMetadataQuery>();
	List<String> mType = Lists.newArrayList(metaTypes);

	int iterateCount = mType.size() / MAX_BATCH_SIZE;

	System.out.println("meta size = " + mType.size());

	for (int i = 0; i < mType.size(); i++)
	{
	    ListMetadataQuery query = new ListMetadataQuery();
	    query.setType(mType.get(i));
	    queries.add(query);

	    System.out.println(i + " " + mType.get(i));

	    if (i < iterateCount * MAX_BATCH_SIZE)
	    {
		if (queries.size() % MAX_BATCH_SIZE == 0)
		{
		    List<ListMetadataQuery> batchQueries = new ArrayList<ListMetadataQuery>(queries);
		    queries.clear();

		    Future<List<FileProperties>> future = executor.submit(new ListMetadataTask(conn, version, batchQueries));
		    futures.add(future);
		}
	    }
	    else
		if (i == mType.size() - 1)
		{
		    List<ListMetadataQuery> batchQueries = new ArrayList<ListMetadataQuery>(queries);
		    queries.clear();

		    Future<List<FileProperties>> future = executor.submit(new ListMetadataTask(conn, version, batchQueries));
		    futures.add(future);
		}

	}

	for (Future<List<FileProperties>> future : futures)
	{
	    List<FileProperties> list;

	    try
	    {
		list = future.get();
		all.addAll(list);
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	}

	return all;
    }

    private class ListMetadataTask implements Callable<List<FileProperties>>
    {
	private MetadataConnection conn;
	private double version;
	private List<ListMetadataQuery> queries;

	public ListMetadataTask(MetadataConnection conn, double version, List<ListMetadataQuery> queries)
	{
	    this.conn = conn;
	    this.version = version;
	    this.queries = queries;
	}

	public List<FileProperties> call() throws ConnectionException
	{
	    System.out.println("query from SF : " + queries.size());

	    FileProperties[] fp = conn.listMetadata(queries.toArray(new ListMetadataQuery[queries.size()]), version);

	    return Arrays.asList(fp);
	}
    }

    public static List<String> getMetaDataType()
    {
	List<String> list = new ArrayList<String>();

	Reflections reflections = new Reflections("com.sforce.soap.metadata");
	Set<Class<? extends Metadata>> subTypes = reflections.getSubTypesOf(Metadata.class);

	// sort the name
	List<Class<? extends Metadata>> myList = new ArrayList<Class<? extends Metadata>>(subTypes);
	Collections.sort(myList, new Comparator<Class<? extends Metadata>>() {

	    @Override
	    public int compare(Class<? extends Metadata> o1, Class<? extends Metadata> o2)
	    {
		return o1.getSimpleName().compareTo(o2.getSimpleName());
	    }
	});

	for (Class<? extends Metadata> class1 : myList)
	{
	    list.add(class1.getSimpleName());
	}

	return list;
    }

    public List<FileProperties> filter(Set<String> prefix, List<FileProperties> fp, Set<String> excludedType)
    {
	List<FileProperties> filterList = new ArrayList<FileProperties>();

	for (FileProperties f : fp)
	{
	    String name = f.getFullName();
	    String type = f.getType();

	    if (excludedType.contains(type))
	    {
		filterList.add(f);
	    }

	    if (match(prefix, name))
	    {
		filterList.add(f);
	    }
	}

	return filterList;
    }

    private boolean match(Set<String> prefixSet, String name)
    {
	for (String prefix : prefixSet)
	{
	    if (name.startsWith(prefix))
		return true;
	}

	return false;
    }

    public void shutdown()
    {
	executor.shutdown();
    }

    public static MetadataConnection login() throws ConnectionException
    {
	final String USERNAME = "gheewooi.ong@domain.com.dev01";
	final String PASSWORD = "";
	final String URL = "https://test.salesforce.com/services/Soap/c/32.0";
	final LoginResult loginResult = LoginUtil.loginToSalesforce(USERNAME, PASSWORD, URL);
	return LoginUtil.createMetadataConnection(loginResult);
    }

    public static void main(String[] args) throws ConnectionException, IOException
    {
	Set<String> metaTypes = new HashSet<String>();
	metaTypes.add("Queue");
	metaTypes.add("EmailFolder");
	metaTypes.add("EmailTemplate");

	Set<String> excludes = new HashSet<String>();
	excludes.add("Translations");

	List<String> list = getMetaDataType();
	for (String type : list)
	{
	    // System.out.println(type);
	    // metaTypes.add(type);
	}

	MetadataConnection conn = login();
	MetadataReader reader = new MetadataReader();

	Long start = System.currentTimeMillis();

	List<FileProperties> fp = reader.getFileList(conn, metaTypes, 31.0);

	Long end = System.currentTimeMillis();

	System.out.println("Get meta from SF time : " + (end - start));

	Set<String> preFix = new HashSet<String>();
	preFix.add("BPAS");

	List<FileProperties> filterList = reader.filter(preFix, fp, excludes);

	PackageXmlGenerator gen = new PackageXmlGenerator();
	String content = gen.generate(metaTypes, filterList, 31.0);
	gen.generateFile("package.xml", content);

	reader.shutdown();
    }
}
