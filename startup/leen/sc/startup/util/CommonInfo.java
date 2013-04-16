package leen.sc.startup.util;

import java.io.File;

public class CommonInfo {
	public static String LEEN_BASE = System.getProperty("user.dir");
	public final static String SERVER = "LEEN";
	public final static String WEB_INF = File.separator + "WEB-INF";
	public final static String LIB = File.separator + "lib";
	public final static String CLASSES = File.separator + "classes";
	public static final String CONTENT_TYPE = "content-type";
	public static final String[] STATIC_EXTS = new String[] { ".html", ".js",
			".css", "jpg", "jpeg", "gif", "png" };
	public static final long DEFAULT_ASYNC_TIMEOUT = 10000;

	public static class FORWARD_PARAM {
		public static final String REQUEST_URI = "java.servlet.forward.request_uri";
		public static final String CONTEXT_PATH = "java.servlet.forward.context_path";
		public static final String SERVLET_PATH = "java.servlet.forward.servlet_path";
		public static final String PATH_INFO = "java.servlet.forward.path_info";
		public static final String QUERY_STRING = "java.servlet.forward.query_string";
	}

	public static class INCLUDE_PARAM {
		public static final String CONTEXT_PATH = "java.servlet.include.request_uri";
		public static final String REQUEST_URI = "java.servlet.include.context_path";;
		public static final String SERVLET_PATH = "java.servlet.include.servlet_path";
		public static final String PATH_INFO = "java.servlet.include.path_info";
		public static final String QUERY_STRING = "java.servlet.include.query_string";
		public static final String URI = "java.servlet.include.uri";
	}
}
