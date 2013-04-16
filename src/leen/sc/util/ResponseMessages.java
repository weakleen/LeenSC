package leen.sc.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ResponseMessages {
	public static String exception(Throwable throwable){
		StringWriter strWriter=new StringWriter();
		PrintWriter out=new PrintWriter(strWriter);
		out.println("500 Internal Server  Error");
		out.println();
		throwable.printStackTrace(out);
		return strWriter.toString();
	} 
	
	public static String notFound(String uri){
		return "404 Not Found "+uri+"\n";
	}

}
