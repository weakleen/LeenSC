package leen.sc.test.performance.util;

import leen.sc.Leen;

import org.apache.log4j.Logger;

public class TestClass1{
	private static Logger log = Logger.getLogger(TestClass1.class);
	
	
	private String uri;
	private String servletPath;
	private String contextPath;
	private String pathInfo;
	private String queryString;
	private String suburi;
	private String folder;
	public String getUri() {
		return uri;
	}
	public String getServletPath() {
		return servletPath;
	}
	public String getContextPath() {
		return contextPath;
	}
	public String getPathInfo() {
		return pathInfo;
	}
	public String getQueryString() {
		return queryString;
	}
	public String getSuburi() {
		return suburi;
	}
	public String getFolder() {
		return folder;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	public void setSuburi(String suburi) {
		this.suburi = suburi;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	
}