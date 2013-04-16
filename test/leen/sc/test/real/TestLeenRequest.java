package leen.sc.test.real;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import leen.sc.RequestException;
import leen.sc.container.LeenContext;
import leen.sc.request.LeenRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author leen 本测试用例用于测试LeenRequest，目前测试了以下功能： （1.）Post方法提交表单数据的解析
 *         （2.）Get方法提交queryString的解析 （3.）InputStream读取request body
 *         （4.）reader读取request body 目前，测试主要考虑的是正常调用情况下的功能执行，未考虑异常调用。
 */
public class TestLeenRequest {

	private LeenRequest req;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testParseGetData() throws RequestException, IOException {
		String msg = "GET /abc?name=leen&pwd=123 HTTP/1.1\r\n" + "\r\n";
		req = mockRequest(msg, "");
		assertEquals("leen", req.getParameter("name"));
		assertEquals("123", req.getParameter("pwd"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testParsePostedData() throws RequestException, IOException {
		String msgBody = "name=" + URLEncoder.encode("李洋")
				+ "&pwd=111&title=god";
		System.out.println("msg body:" + msgBody);
		String msg = "POST /?code=0111 HTTP/1.1\r\n"
				+ "Content-Type: application/x-www-form-urlencoded\r\n"
				+ "Content-Length: " + msgBody.getBytes().length + "\r\n"
				+ "\r\n" + msgBody;
		req = mockRequest(msg, "");
		assertEquals("0111", req.getParameter("code"));
		assertEquals("李洋", req.getParameter("name"));
		assertEquals("111", req.getParameter("pwd"));
		assertEquals("god", req.getParameter("title"));
	}

	@Test
	public void testParsePostedDataBoundary1() throws RequestException,
			IOException {
		String msg = "POST /?code=0111 HTTP/1.1\r\n"
				+ "Content-Type: application/x-www-form-urlencoded\r\n"
				+ "Content-Length: 0\r\n" + "\r\n";
		req = mockRequest(msg, "/");
	}

	@Test
	public void testContentLengthAbsence() throws RequestException, IOException {
		String msg = "POST / HTTP/1.1\r\n" + "\r\n"
				+ "name=leen&pwd=111&title=god";
		try {
			req = mockRequest(msg, "");
			fail();
		} catch (RequestException e) {
			System.out
					.println("caught exception for content-length absence in post data");
		}
	}

	@Test
	public void testInputStream() throws IOException, RequestException {
		int sourceSize = 1024 * 1024;
		byte[] source = new byte[sourceSize];
		new Random().nextBytes(source);
		String msg = "POST / HTTP/1.1\r\n"
				+ "Content-Type: application/x-www-form-urlencoded\r\n"
				+ "Content-Length: " + sourceSize + "\r\n" + "\r\n";

		List<Byte> list = new ArrayList<Byte>();

		for (Byte b2 : msg.getBytes()) {
			list.add(b2);
		}

		for (byte b : source) {
			list.add(b);
		}

		byte[] bs = new byte[list.size()];
		for (int i = 0; i < bs.length; i++)
			bs[i] = list.get(i);

		req = mockRequest(bs, "");
		InputStream in = req.getInputStream();
		assertEquals(sourceSize, in.available());
		for (int i = 0; i < sourceSize; i++)
			source[i] = (byte) in.read();
	}

	@Test
	public void testReader() throws RequestException, IOException {
		String msgBody = "Hello Every One,我叫我李洋！";
		String msg = "POST / HTTP/1.1\r\n" + "Content-Length: "
				+ msgBody.getBytes().length + "\r\n" + "\r\n" + msgBody;
		req = mockRequest(msg, "");
		BufferedReader reader = req.getReader();
		StringBuffer buf = new StringBuffer();
		char c;
		int i;
		while ((i = reader.read()) != -1) {
			c = (char) i;
			buf.append(c);
		}
		assertEquals(msgBody, buf.toString());

	}

	private LeenRequest mockRequest(String msg, String servletPath)
			throws RequestException, IOException {
		return mockRequest(msg.getBytes(), servletPath);
	}

	private LeenRequest mockRequest(byte[] buf, String servletPath)
			throws RequestException, IOException {
		InputStream in = new ByteArrayInputStream(buf);
		LeenRequest req = new LeenRequest(in);
		req.parse();
		req.setServletPath(servletPath);
		req.setContext(new LeenContext("webapps", "/"));
		return req;
	}

}
