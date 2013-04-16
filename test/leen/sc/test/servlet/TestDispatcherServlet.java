package leen.sc.test.servlet;

import java.io.IOException;
import static leen.sc.test.real.TestDispatcher.*;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.startup.util.CommonInfo;
import leen.sc.test.formal.TestContext;
import leen.sc.test.real.TestDispatcher;

import org.apache.log4j.Logger;

public class TestDispatcherServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(TestDispatcherServlet.class);
	public static boolean getByName=false;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("TestDispatcherServlet invoked");
		
		TestContext.exeMap.put("testRD.executed",true);
		
		if(!getByName&&req.getDispatcherType()==DispatcherType.FORWARD){
			boolean rs0=req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI).equals(TestDispatcher.ORIGIN_URI.getURI());
			boolean rs1=req.getAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH).equals(TestDispatcher.ORIGIN_URI.getContextPath());
			boolean rs2=req.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH).equals(TestDispatcher.ORIGIN_URI.getServletPath());
			boolean rs3=req.getAttribute(RequestDispatcher.FORWARD_PATH_INFO)==TestDispatcher.ORIGIN_URI.getPathInfo()||req.getAttribute(RequestDispatcher.FORWARD_PATH_INFO).equals(TestDispatcher.ORIGIN_URI.getPathInfo());
			boolean rs4=req.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING).equals(TestDispatcher.ORIGIN_URI.getQueryString());
			
			TestContext.exeMap.put("forward.param", rs0&&rs1&&rs2&&rs3&&rs4);
			
			boolean rs5=req.getRequestURI().equals(TestDispatcher.NEW_URI.getURI());
			boolean rs6=req.getContextPath().equals(TestDispatcher.NEW_URI.getContextPath());
			boolean rs7=req.getServletPath().equals(TestDispatcher.NEW_URI.getServletPath());
			boolean rs8=req.getPathInfo()==TestDispatcher.NEW_URI.getPathInfo()||req.getPathInfo().equals(TestDispatcher.NEW_URI.getPathInfo());
			boolean rs9=req.getQueryString()==TestDispatcher.NEW_URI.getQueryString()||req.getQueryString().equals(TestDispatcher.NEW_URI.getQueryString());
			TestContext.exeMap.put("forward.path", rs5&&rs6&&rs7&&rs8&&rs9);
		}
	}
}
