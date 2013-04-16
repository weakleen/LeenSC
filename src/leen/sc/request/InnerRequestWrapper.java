package leen.sc.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import leen.sc.util.Retriever;

public class InnerRequestWrapper extends HttpServletRequestWrapper {
	
	private RequestURI uri; 
	
	public InnerRequestWrapper(HttpServletRequest request) {
		super(request);
		uri=Retriever.retrieveRequest(request).getCurrentURI();
	}

	public void setUri(RequestURI uri){
		if(uri==null)
			throw new IllegalArgumentException("uri required");
		this.uri=uri;
	}
	
	@Override
	public String getRequestURI() {
		return uri.getURI();
	}
	
	@Override
	public StringBuffer getRequestURL() {
		StringBuffer buffer=new StringBuffer();
		buffer.append(getRequestURI());
		return buffer;
	}
	
	@Override
	public String getServletPath() {
		return uri.getServletPath();
	}
	
	@Override
	public String getContextPath() {
		return uri.getContextPath();
	}
	
	@Override
	public String getPathInfo() {
		return uri.getPathInfo();
	}
	
	@Override
	public String getQueryString() {
		return uri.getQueryString();
	}
}
