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
	 * �ò������ڲ���Request Uri�Ƿ��ܹ���ɶԲ�������ļ�⣬���������ԭʼuri��
	 *  context path��servlet path��
	 */
	@Test
	public void testCheck() {
		// fail case 1��uri������/��ʼ
		try {
			uri.setRawURI("Mock/index.jsp");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 2��context path������/��β
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("/Test/");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 3��context path������/��ʼ
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("Test");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}

		// fail case 4��uri������context path��ʼ
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setContextPath("/Test");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}
		// fail case 5��servlet path����Ϊ�ջ�����/��ʼ
		try {
			uri.recycle();
			uri.setRawURI("/Mock/index.jsp");
			uri.setServletPath("index.jsp");
			fail();
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			fail();
		}
		
		// fail case 6��uri������context path+servlet path��ʼ
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
		
		// success case 1��most common
		uri.recycle();
		uri.setRawURI("/Mock/index.jsp?name=leen&pwd=123");
		uri.setContextPath("/Mock");
		uri.setServletPath("/index.jsp");
		
		// success case 2��root context path
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
