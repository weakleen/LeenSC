package leen.sc.request;

import org.apache.log4j.Logger;

public class RequestURI {

	private static Logger log = Logger.getLogger(RequestURI.class);

	private String uri;
	private String servletPath;
	private String contextPath;
	private String pathInfo;
	private String queryString;
	private String suburi;
	private String folder;

	public static int INSTANCE_COUNT = 0;

	private void parseUriAndQueryString(String rawUri) {
		int idx = rawUri.indexOf("?");
		if (idx == -1)
			uri = rawUri;
		else {
			uri = rawUri.substring(0, idx);
			queryString = rawUri.substring(idx + 1);
			log.debug("parsed query string :" + queryString);
		}
	}

	private void parsePathInfo() {
		if (uri == null || contextPath == null || servletPath == null)
			return;
		pathInfo = uri.replaceFirst(contextPath, "").replaceFirst(servletPath,
				"");
		pathInfo = pathInfo.equals("") ? null : pathInfo;
	}

	private void parseSubUrl() {
		if (uri == null || contextPath == null)
			return;
		suburi = uri.substring(contextPath.length());
	}

	// folder不包含contex path
	private void parseFolder() {
		if (suburi == null)
			return;
		int idx = suburi.lastIndexOf('/');
		if (idx == -1)
			// 未知原因
			throw new RuntimeException();
		folder = suburi.substring(0, idx + 1);
	}

	/**
	 * 该方法用于检测输入的uri、context path、servlet path是否合法，检测的规则为: 1、uri必须以/开始
	 * 2、uri必须以context path+servlet path开始 3、context path为空字符串，或者，以/开始， 非/结尾
	 * 4、servlet path为空字符串，或者，以/开始
	 */
	private void check() {
		if (uri != null)
			if (!uri.startsWith("/"))
				throw new IllegalArgumentException("uri must start with /");
		if (contextPath != null) {
			if (!contextPath.equals("")
					&& !(contextPath.startsWith("/") && !contextPath
							.endsWith("/"))) {
				throw new IllegalArgumentException("illegal context path "
						+ contextPath);
			}
			if (uri != null && !uri.startsWith(contextPath))
				throw new IllegalArgumentException(
						"uri must start with context path");
		}
		if (servletPath != null) {
			if (!servletPath.equals("") && !servletPath.startsWith("/"))
				throw new IllegalArgumentException(
						"servlet path must equals empty string or start with /");
			if (contextPath != null) {
				if (uri != null && !uri.startsWith(contextPath + servletPath))
					throw new IllegalArgumentException(
							"uri must starts with context path plus servlet path");
			}
		}

	}

	public String getURI() {
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

	public String getSubUrl() {
		return suburi;
	}

	public String getFolder() {
		return folder;
	}

	// 传入浏览器发送过来的原始URI
	public void setRawURI(String rawUri) {
		parseUriAndQueryString(rawUri);
		check();
		parseSubUrl();
		parsePathInfo();
		parseFolder();
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
		check();
		parsePathInfo();
	}

	public void setContextPath(String contextPath) {
		if(contextPath==null)
			throw new IllegalArgumentException("context path required");
		this.contextPath = contextPath.equals("/") ? "" : contextPath;
		check();
		parseSubUrl();
		parsePathInfo();
		parseFolder();
	}

	// REF K004.5
	public String toAbsolute(String relative) {
		if (relative == null)
			throw new IllegalArgumentException();
		if (relative.startsWith("/")) {
			if (contextPath == null)
				throw new IllegalStateException();
			return contextPath + relative;
		} else {
			if (folder == null)
				throw new IllegalStateException();
			return contextPath + folder + relative;
		}
	}

	public void recycle() {
		this.uri = null;
		this.contextPath = null;
		this.servletPath = null;
		this.pathInfo = null;
	}

}
