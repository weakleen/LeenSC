package leen.sc.container;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.ConfigException;
import leen.sc.filter.LeenFilterManager;
import leen.sc.filter.LeenFilterWrapper;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;
import leen.sc.servlet.ServletWrapper;
import leen.sc.servlet.ServletWrapperManager;

public interface ILeenContext extends ServletContext{
	
	public void init() throws ConfigException;

	public void leen(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

	public String getContextPath();

	public void setContextPath(String contextPath);

	public WelcomeManager getWelcomeManager();
	
	public void setWelcomeManager(WelcomeManager welcomeManager);
	
	public void addWrapper(ServletWrapper wrapper);

	public void setFilterWrapperList(List<LeenFilterWrapper> filterWrapperList);

	public void setFilterManager(LeenFilterManager filterMgr);
	
	public ServletWrapperManager getSWM();
	
	public void setSWM(ServletWrapperManager swm);
	
	public HttpServlet getServletInternal(String name) throws ServletException;

	public Executor getExecutor();
	
	public void setExecutor(Executor executor);

	public void request(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException;
	
}