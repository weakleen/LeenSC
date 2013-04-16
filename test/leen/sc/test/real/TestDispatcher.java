package leen.sc.test.real;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import leen.sc.RequestException;
import leen.sc.container.LeenContext;
import leen.sc.dispatcher.LeenRequestDispatcher;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.test.formal.TestContext;
import leen.sc.test.real.util.MockContextFactory;
import leen.sc.test.real.util.TestUtils;
import leen.sc.test.servlet.TestDispatcherServlet;

import org.junit.Before;
import org.junit.Test;

public class TestDispatcher {
	public static RequestURI ORIGIN_URI=new RequestURI();
	public static RequestURI NEW_URI=new RequestURI();
	public static String NEW_SERVLET_PATH="/testRD";
	public static String WRAPPER_URI="/leen/wrapper";
	public static String QUERYSTRING_URI="/leen/querystring";
	public static String SERVLET_NAME="testRD";
	
	public static String[] PARAM_NAMES=new String[]{"name","pwd","code"};
	public static String[] OLD_VALUES=new String[]{"leen","123","1"};
	public static String[] NEW_VALUES=new String[]{"lyq","456","2"};
	
	
	static{
		ORIGIN_URI.setRawURI("/Mock/leen/test1?"+genQueryString(true));
		ORIGIN_URI.setContextPath("/Mock");
		ORIGIN_URI.setServletPath("/leen/test");

		NEW_URI.setRawURI("/Mock/leen/testRD?code=1");
		NEW_URI.setContextPath("/Mock");
		NEW_URI.setServletPath("/leen/testRD");
	}
	
	private LeenRequestDispatcher dispatcher;
	private LeenContext ctx;
	
	@Before
	public void setUp() throws Exception {
		TestContext.exeMap.clear();
		ctx=MockContextFactory.getInstance();
		
		ctx.init();
		dispatcher=new LeenRequestDispatcher();
		dispatcher.setContext(ctx);
		dispatcher.setPath(NEW_URI.getSubUrl()+"?"+NEW_URI.getQueryString());
	}
	
/*	@Test
	public void test() throws Exception{
		LeenContext ctx=new LeenContext("webapps","/Mock");
		ctx.init();
		dispatcher=new LeenRequestDispatcher("/test2");
		dispatcher.setContext(ctx);
		String msg="GET /Mock/test1 HTTP/1.1\r\n\r\n";
		dispatcher.forward(mockRequest(msg, "test1"), mockResponse());
		assertTrue(ExeFlags.flags.get("conf2"));
	}
*/
	@Test
	public void testQueryString() throws Exception{
		LeenRequest req=TestUtils.mockRequest(ORIGIN_URI, ctx);
		assertNotNull(req.getParameter(PARAM_NAMES[0]));
		System.out.println("queryString uri:"+QUERYSTRING_URI+"?"+genQueryString(false));
		ctx.getRequestDispatcher(QUERYSTRING_URI+"?"+genQueryString(false)).forward(req, TestUtils.mockResponse(req));
		assertTrue(TestUtils.isExecuted("querystring"));
		boolean qRS=false;
		for(int i=0;i<PARAM_NAMES.length;i++){
			qRS=qRS||req.getParameter(PARAM_NAMES[i]).equals(NEW_VALUES[i]);
		}
		assertFalse(qRS);
	}
	
	private static String genQueryString(boolean old){
		StringBuilder builder=new StringBuilder();
		for(int i=0;i<PARAM_NAMES.length;i++){
			String value=null;
			if(old)
				value=OLD_VALUES[i];
			else
				value=NEW_VALUES[i];
			builder.append(PARAM_NAMES[i]+"="+value);
			if(i<PARAM_NAMES.length-1)
				builder.append('&');
		}
		return builder.toString();
	}
	
	@Test
	public void testForwardByUri() throws ServletException, IOException, RequestException{
		TestDispatcherServlet.getByName=false;
		LeenRequest request=TestUtils.mockRequest(ORIGIN_URI, ctx);
		dispatcher.forward(request, TestUtils.mockResponse(request));
		assertTrue(TestContext.exeMap.get("testRD.executed"));
		assertTrue(TestContext.exeMap.get("forward.param"));
		assertTrue(TestContext.exeMap.get("forward.path"));
	}
	
	@Test
	public void testGetByName() throws ServletException, IOException, RequestException{
		TestDispatcherServlet.getByName=true;
		LeenRequest request =TestUtils.mockRequest(ORIGIN_URI, ctx);
		ctx.getNamedDispatcher(SERVLET_NAME).forward(TestUtils.mockRequest(ORIGIN_URI, ctx), TestUtils.mockResponse(request));
		assertTrue(TestContext.exeMap.get("testRD.executed"));
	}
	
	@Test
	public void testGetByRelativePath() throws Exception{
		LeenRequest req=TestUtils.mockRequest(ORIGIN_URI, ctx);
		req.getRequestDispatcher("testRD"+"?"+NEW_URI.getQueryString()).forward(req, TestUtils.mockResponse(req));
		assertTrue(TestContext.exeMap.get("testRD.executed"));
	}
	
	@Test
	public void testGetByPath() throws Exception{
		LeenRequest req=TestUtils.mockRequest(ORIGIN_URI, ctx);
		req.getRequestDispatcher(NEW_URI.getSubUrl()+"?"+NEW_URI.getQueryString()).forward(req, TestUtils.mockResponse(req));
		assertTrue(TestContext.exeMap.get("testRD.executed"));
	}
	
	@Test
	public void testWrapper() throws Exception{
		LeenRequest req=TestUtils.mockRequest(ORIGIN_URI, ctx);
		ServletRequest reqWrapper=new MyRequestWrapper(req);
		ServletResponse resWrapper=new MyResponseWrapper(TestUtils.mockResponse(req));
		ctx.getRequestDispatcher(WRAPPER_URI).forward(reqWrapper, resWrapper);
		assertTrue(TestContext.exeMap.get("testWrapperExe"));
		assertTrue(TestContext.exeMap.get("requestWrapper"));
		assertTrue(TestContext.exeMap.get("responseWrapper"));
	}
	
	@Test
	//include»ù±¾²âÊÔ
	public void testInlcude() throws Exception{
		dispatcher.setPath(NEW_URI.getSubUrl()+"?"+NEW_URI.getQueryString());
		LeenRequest request=TestUtils.mockRequest(ORIGIN_URI, ctx);
		dispatcher.include(request, TestUtils.mockResponse(request));
		assertTrue(TestContext.exeMap.get("testRD.executed"));
	}
}

class MyRequestWrapper extends HttpServletRequestWrapper{

	public MyRequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	@Override
	public String getParameter(String name) {
		TestContext.exeMap.put("requestWrapper", true);
		return super.getParameter(name);
	}
	
}

class MyResponseWrapper extends HttpServletResponseWrapper{

	public MyResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		TestContext.exeMap.put("responseWrapper", true);
		return super.getWriter();
	}
}