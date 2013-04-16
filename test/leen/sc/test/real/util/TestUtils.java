package leen.sc.test.real.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRegistration;

import leen.sc.RequestException;
import leen.sc.container.ILeenContext;
import leen.sc.container.LeenContext;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.BufferManager;
import leen.sc.response.BufferPool;
import leen.sc.response.LeenResponse;
import leen.sc.response.LeenServletOutputStream;
import leen.sc.test.servlet.MockAsyncTimout;

import org.apache.log4j.Logger;

public class TestUtils {
	
	public static final String ASYNC_TIMEOUT_URL="/aysncTimeout"; 
	public static Map<String, Boolean> exeMap = new HashMap<String, Boolean>();

	private static BufferManager bufferManager=new BufferPool();
	
	private static final Logger log=Logger.getLogger(TestUtils.class);
	
	public static LeenRequest mockRequest(String msg, String servletPath)
			throws RequestException, IOException {
		return mockRequest(msg.getBytes(), servletPath);
	}
	
	public static LeenRequest mockRequest() throws Exception{
		RequestURI uri=new RequestURI();
		uri.setRawURI("/Mock/test");
		uri.setServletPath("/test");
		ILeenContext ctx=MockContextFactory.getInstance();
		LeenRequest req=mockRequest(uri, ctx);
		return req;
	}

	public static LeenRequest mockRequest(byte[] buf, String servletPath)
			throws RequestException, IOException {
		InputStream in = new ByteArrayInputStream(buf);
		LeenRequest req = new LeenRequest(in);
		req.parse();
		req.setServletPath(servletPath);
		req.setContext(new LeenContext("webapps", "/"));
		return req;
	}
	

	public static LeenRequest mockRequest(byte[] buf)
			throws RequestException, IOException {
		InputStream in = new ByteArrayInputStream(buf);
		LeenRequest req = new LeenRequest(in);
		req.parse();
		req.setContext(MockContextFactory.getInstance());
		return req;
	}

	public static LeenRequest mockRequest(RequestURI uri,ILeenContext context)
			throws RequestException, IOException {
		String msg = "GET "+uri.getURI()+"?"+uri.getQueryString()+" HTTP/1.1\r\n" + "host: localhost\r\n";
		InputStream in = new ByteArrayInputStream(msg.getBytes());
		LeenRequest req = new LeenRequest(in);
		req.parse();
		req.setServletPath(uri.getServletPath());
		req.setContext(context);
		return req;
	}

	public static RequestURI MockURI(String path){
		RequestURI URI=new RequestURI();
		URI.setRawURI("/Mock"+path);
		URI.setContextPath("/Mock");
		return URI;
	}
	
	public static LeenResponse mockResponse(LeenRequest req) {
		
		LeenResponse res=new LeenResponse(mockServletOut());
		res.setRequest(req);
		req.setResponse(res);
		return res;
	}
	
	public static LeenServletOutputStream mockServletOut(){
		ByteArrayOutputStream originOut=new ByteArrayOutputStream();
		LeenServletOutputStream servletOut=new LeenServletOutputStream(bufferManager, bufferManager.getBuffer(), originOut);
		return servletOut;
	}
	
	public static void registerServlets(ILeenContext context){
		ServletRegistration reg=context.addServlet("aysncTimeout", MockAsyncTimout.class);
		reg.addMapping(ASYNC_TIMEOUT_URL);
	}
	
	public static void setExecuted(String key){
		exeMap.put(key, true);
	}
	
	public static boolean isExecuted(String key){
		if(exeMap.containsKey(key))
			return true;
		return false;
	}

	public static LeenResponse mockResponse() throws Exception {
		return mockResponse(mockRequest());
	}
	
}
