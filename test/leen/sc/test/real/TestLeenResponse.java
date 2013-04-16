package leen.sc.test.real;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;
import leen.sc.test.real.util.TestUtils;

import org.junit.Before;

public class TestLeenResponse extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	// sendRedirect测试，参考C004.2
	public void testRedirect() throws Exception {
		String host = "weakleen.com";
		String msg = "GET /Mock/test HTTP/1.1\r\n" + "host: " + host + "\r\n";
		LeenRequest request = TestUtils.mockRequest(msg.getBytes());

		// absolute path
		LeenResponse response = TestUtils.mockResponse(request);
		response.sendRedirect("http://www.baidu.com");
		assertEquals("http://www.baidu.com", response.getHeader("Location"));
		
//		测试response关闭，状态码
		assertEquals(HttpServletResponse.SC_FOUND,response.getStatus());
		assertTrue(response.isClosed());
		
		// relative path with leading slash
		response = TestUtils.mockResponse(request);
		response.sendRedirect("/test1");
		assertEquals("http://" + host + "/Mock/test1",
				response.getHeader("Location"));
		// relative path without leading slash
		response = TestUtils.mockResponse(request);
		response.sendRedirect("test1");
		assertEquals("http://" + host + "/Mock/test1",
				response.getHeader("Location"));
		
		// 测试sendRedirect不能在response提交之后调用
		response = TestUtils.mockResponse(request);
		response.flushBuffer();
		try {
			response.sendRedirect("test1");
			fail();
		} catch (IllegalStateException e) {
		}
	}
	
//	测试编码设置，参考K004.6
	public void testEncoding() throws Exception{
		LeenResponse response=TestUtils.mockResponse();
//		测试基本功能，测试不创建ContentType
		String encoding="GBK";
		response.setCharacterEncoding(encoding);
		assertEquals(encoding,response.getCharacterEncoding());
		assertNull(response.getContentType());
//		测试getWriter方法调用之后，该方法不起作用
		response.getWriter();
		String encoding2="GB2312";
		response.setCharacterEncoding(encoding2);
		assertEquals(encoding,response.getCharacterEncoding());
//		测试response提交之后，该方法不起作用
		response=TestUtils.mockResponse();
		response.setCharacterEncoding(encoding);
		response.flushBuffer();
		response.setCharacterEncoding(encoding2);
		assertEquals(encoding,response.getCharacterEncoding());
//		测试使用ContentType设置Encoding
		response=TestUtils.mockResponse();
		response.setContentType("text/html;charset="+encoding);
		assertEquals(encoding,response.getCharacterEncoding());
	}
	
//	测试ContentType设置，参考K004.7.1
	public void testContentType() throws Exception{
		LeenResponse response=TestUtils.mockResponse();
		assertNull(response.getContentType());
		String ct="text/html;charset=gb2312";
		String ct2="text/css;charset=gb2312";
		String ct3="text/css";
		
//		测试基本功能
		response.setContentType(ct);
		assertEquals(ct, response.getContentType());
		
//		测试使用getHeader、setHeader获取、设置contentType
		assertEquals(ct, response.getHeader("Content-Type"));
		response.setHeader("content-type",ct2);
		assertEquals(ct2,response.getContentType());
		
//		测试prepareResponse时生成contentType(TODO)
		response=TestUtils.mockResponse();
		response.flushBuffer();
		
//		通过设置Encoding改变contentType
		response=TestUtils.mockResponse();
		response.setContentType(ct2);
		response.setCharacterEncoding("UTF-8");
		assertEquals(ct2,response.getContentType());
		response.setContentType(ct3);
		assertEquals(ct3+";charset=UTF-8",response.getContentType());
	}
}
