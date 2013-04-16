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

	// sendRedirect���ԣ��ο�C004.2
	public void testRedirect() throws Exception {
		String host = "weakleen.com";
		String msg = "GET /Mock/test HTTP/1.1\r\n" + "host: " + host + "\r\n";
		LeenRequest request = TestUtils.mockRequest(msg.getBytes());

		// absolute path
		LeenResponse response = TestUtils.mockResponse(request);
		response.sendRedirect("http://www.baidu.com");
		assertEquals("http://www.baidu.com", response.getHeader("Location"));
		
//		����response�رգ�״̬��
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
		
		// ����sendRedirect������response�ύ֮�����
		response = TestUtils.mockResponse(request);
		response.flushBuffer();
		try {
			response.sendRedirect("test1");
			fail();
		} catch (IllegalStateException e) {
		}
	}
	
//	���Ա������ã��ο�K004.6
	public void testEncoding() throws Exception{
		LeenResponse response=TestUtils.mockResponse();
//		���Ի������ܣ����Բ�����ContentType
		String encoding="GBK";
		response.setCharacterEncoding(encoding);
		assertEquals(encoding,response.getCharacterEncoding());
		assertNull(response.getContentType());
//		����getWriter��������֮�󣬸÷�����������
		response.getWriter();
		String encoding2="GB2312";
		response.setCharacterEncoding(encoding2);
		assertEquals(encoding,response.getCharacterEncoding());
//		����response�ύ֮�󣬸÷�����������
		response=TestUtils.mockResponse();
		response.setCharacterEncoding(encoding);
		response.flushBuffer();
		response.setCharacterEncoding(encoding2);
		assertEquals(encoding,response.getCharacterEncoding());
//		����ʹ��ContentType����Encoding
		response=TestUtils.mockResponse();
		response.setContentType("text/html;charset="+encoding);
		assertEquals(encoding,response.getCharacterEncoding());
	}
	
//	����ContentType���ã��ο�K004.7.1
	public void testContentType() throws Exception{
		LeenResponse response=TestUtils.mockResponse();
		assertNull(response.getContentType());
		String ct="text/html;charset=gb2312";
		String ct2="text/css;charset=gb2312";
		String ct3="text/css";
		
//		���Ի�������
		response.setContentType(ct);
		assertEquals(ct, response.getContentType());
		
//		����ʹ��getHeader��setHeader��ȡ������contentType
		assertEquals(ct, response.getHeader("Content-Type"));
		response.setHeader("content-type",ct2);
		assertEquals(ct2,response.getContentType());
		
//		����prepareResponseʱ����contentType(TODO)
		response=TestUtils.mockResponse();
		response.flushBuffer();
		
//		ͨ������Encoding�ı�contentType
		response=TestUtils.mockResponse();
		response.setContentType(ct2);
		response.setCharacterEncoding("UTF-8");
		assertEquals(ct2,response.getContentType());
		response.setContentType(ct3);
		assertEquals(ct3+";charset=UTF-8",response.getContentType());
	}
}
