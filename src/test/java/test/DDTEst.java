package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.reflections.Reflections;

import com.github.autoforce.controller.Controller;
import com.sforce.soap.metadata.Metadata;

public class DDTEst {

	public static void main(String[] args) throws IOException {
		
//		FileWriter out = new FileWriter("code.txt");
//		
//		BufferedReader in = new BufferedReader(new FileReader("dd.xml"));
//		
//		String line = null;
//		
//		while((line = in.readLine()) != null)
//		{
//			out.append("\'" + line + " \'");
//			out.append("\n");
//			
//		}
//		
//		in.close();
//		out.close();
		
//		System.out.println( 2 % 3);
//		
//		int k = "Region de Magallanes y de la Antartica Chilena".length();
//		System.out.println(k);
//		
//		System.out.println(12345 / 1000.000);
		
//		double covered = 6;
//		double uncovered = 16;
//		
//		double percent = (covered / (covered + uncovered)) * 100; 
//		
//		System.out.println(percent);
		
//		Map<String, Object> model = new HashMap<String,Object>();
//		
//		Reflections reflections = new Reflections("com.domain.salesforce.task");
//		Set<Class<?>> clazzes = reflections.getTypesAnnotatedWith(WebServlet.class);
//		
//		for (Class<?> class1 : clazzes) {
//			Annotation[] anns = class1.getAnnotations();
//			
//			for (Annotation annotation : anns) 
//			{			
//				Map<String, String> task = new HashMap<String,String>();
//				
//				if(annotation instanceof Controller )
//				{				
//					Controller con = (Controller) annotation;
//					
//					String name = con.name();
//					String desc = con.description();
//					String url = con.url();
//					
//					
//					task.put("name", name);
//					task.put("desc", desc);	
//					task.put("url", url);
//					
//					model.put(url, task);
//				}				
//				
//			}
//		}
//		
//		System.out.println(model);
		
//		String url = "https://domain--Test.cs30.my.salesforce.com/services/Soap/c/32.0/00Dn0000000DFlb";
//		
//		int d = url.indexOf('/', 8);
//		String sub = url.substring(0,d);
//		
//		System.out.println(sub);
				       
		Date date = null;
		
		try {
			date = new SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.ENGLISH).parse("2015-05-22 05:12:59");
		} catch (ParseException e) {
			
		}
		
        System.out.println("my last = " + date);
	}
	
}
