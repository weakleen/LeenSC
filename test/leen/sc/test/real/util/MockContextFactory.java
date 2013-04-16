package leen.sc.test.real.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletRegistration;

import leen.sc.container.LeenContext;
import leen.sc.container.WelcomeManager;
import leen.sc.request.RequestURI;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.test.servlet.TestDispatcherServlet;
import leen.sc.test.servlet.TestQueryStringServlet;
import leen.sc.test.servlet.TestWrapperServlet;

public class MockContextFactory {
	
	public static RequestURI ORIGIN_URI=new RequestURI();
	public static RequestURI NEW_URI=new RequestURI();
	public static String NEW_SERVLET_PATH="/testRD";
	public static String WRAPPER_URI="/leen/wrapper";
	public static String QUERYSTRING_URI="/leen/querystring";
	public static String SERVLET_NAME="testRD";
	
	static{
		ORIGIN_URI.setRawURI("/Mock/leen/test1");
		ORIGIN_URI.setContextPath("/Mock");
		ORIGIN_URI.setServletPath("/leen/test");

		NEW_URI.setRawURI("/Mock/leen/testRD?code=1");
		NEW_URI.setContextPath("/Mock");
		NEW_URI.setServletPath("/leen/testRD");
	}
	
	
	public static LeenContext getInstance(){
		LeenContext instance=new LeenContext("webapps", "/Mock");
		instance.setSWM(new ServletWrapperManager());
		//×¢²áservlet
		ServletRegistration reg=instance.addServlet(SERVLET_NAME, TestDispatcherServlet.class);
		reg.addMapping(NEW_URI.getSubUrl());
		reg=instance.addServlet("testWrapper", TestWrapperServlet.class);
		reg.addMapping(WRAPPER_URI);
		reg=instance.addServlet("testQueryString", TestQueryStringServlet.class);
		reg.addMapping(QUERYSTRING_URI);
		
		instance.setWelcomeManager(new WelcomeManager());
		
		ThreadPoolExecutor executor=new ThreadPoolExecutor(5, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5));
		instance.setExecutor(executor);
		return instance;
	}
}
