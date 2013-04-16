package leen.sc.test.real;

import junit.framework.TestCase;
import leen.sc.request.RequestURI;

import org.junit.Before;
import org.junit.Test;

public class TestRequestUri extends TestCase {

	private RequestURI uri;

	@Before
	public void setUp() throws Exception {
		uri = new RequestURI();
	}

	/**
	 * 该测试用于测试Request Uri是否能够完成对不法输入的检测，输入包括：原始uri、
	 *  context path、servlet path。
	 */
	@Test
	public void testCheck() {
		// fail case 1：uri必须以/开始
		try {
			uri.setRawURI("Mock/index.jsp");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 2：context path不能以/结尾
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("/Test/");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 3：context path必须以/开始
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("Test");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 4：uri必须以context path开始
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("/Test");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}
		// fail case 5：servlet path必须为空或者以/开始
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setServletPath("index.jsp");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}
		
		// fail case 6：uri必须以context path+servlet path开始
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("/Mock");
			uri.setServletPath("/index1.jsp");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}
		
		// success case 1：most common
		uri.recycle();
		uri.setRawURI("/Mock/index.jsp?name=leen&pwd=123");
		uri.setContextPath("/Mock");
		uri.setServletPath("/index.jsp");
		
		// success case 2：root context path
		uri.recycle();
		uri.setRawURI("/index.jsp");
		uri.setContextPath("");
		uri.setServletPath("/index.jsp");
		
	}

	@Test
	public void test() {
		// test1
		uri.setRawURI("/Mock/index.jsp");
		uri.setContextPath("/Mock");
		uri.setServletPath("/index.jsp");
		assertEquals("/index.jsp", uri.getSubUrl());

		uri.recycle();
		// test2
		uri.setRawURI("/index.jsp");
		uri.setContextPath("");
		uri.setServletPath("/index.jsp");
		assertEquals("/index.jsp", uri.getSubUrl());
	}

}
