package leen.sc.test.real;

import junit.framework.TestCase;
import leen.sc.request.Parameters;

import org.junit.Before;

public class TestParameters extends TestCase {
	private Parameters toTest;

	@Before
	public void setUp() throws Exception {
		toTest = new Parameters();
	}

	public void test() {
		toTest.push("name=leen&pwd=123&code=1");
		assertEquals("leen", toTest.getParameter("name"));
		assertEquals("123", toTest.getParameter("pwd"));
		assertEquals("1", toTest.getParameter("code"));

		toTest.push("name=lyq");
		assertEquals("lyq", toTest.getParameter("name"));
		assertEquals("123", toTest.getParameter("pwd"));
		assertEquals("1", toTest.getParameter("code"));
		assertEquals(2, toTest.getParameters("name").size());

		toTest.pop();
		assertEquals("leen", toTest.getParameter("name"));
		assertEquals("123", toTest.getParameter("pwd"));
		assertEquals("1", toTest.getParameter("code"));
		assertEquals(1, toTest.getParameters("name").size());

	}

	public void testBoundary() {
		try {
			toTest.push(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

}
