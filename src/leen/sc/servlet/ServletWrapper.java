package leen.sc.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import leen.sc.container.Container;
import leen.sc.container.sub.LeenServletRegistration;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;

public interface ServletWrapper extends Container,ServletConfig{

	public void leen(LeenRequest request, LeenResponse response)
			throws ServletException, IOException;

	public void init() throws ServletException;

	public HttpServlet loadServlet() throws ServletException;

	public String getClassName();
	
	public void setServletName(String servletName);

	public void setInitParameters(Map<String, String> initParameters);
	
	public void setInitParameter(String name,String value);
	
	public Map<String,String> getInitParameters();
	
	public void destroy();


}