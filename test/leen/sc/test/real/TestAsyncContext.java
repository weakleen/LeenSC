package leen.sc.test.real;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import junit.framework.TestCase;
import leen.sc.container.LeenContext;
import leen.sc.dispatcher.LeenAsyncContext;
import leen.sc.dispatcher.TimeCycleProcessor;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.LeenResponse;
import leen.sc.startup.util.CommonInfo;
import leen.sc.test.formal.TestContext;
import leen.sc.test.formal.servlet.TestServlet3;
import leen.sc.test.real.util.MockContextFactory;
import leen.sc.test.real.util.TestUtils;
import leen.sc.test.servlet.TestServlet;

import org.junit.Before;
import org.junit.Test;

public class TestAsyncContext extends TestCase {

	LeenAsyncContext ac;
	LeenContext context;
	RequestURI originUri;
	private static final long timeout = 100;
	@Before
	public void setUp() throws Exception {
		if (!TimeCycleProcessor.getInstance().isStarted())
			TimeCycleProcessor.getInstance().startService();

		TestUtils.exeMap.clear();

		context = MockContextFactory.getInstance();
		TestUtils.registerServlets(context);
		originUri = new RequestURI();
		originUri.setContextPath("/Mock");
		originUri.setRawURI("/Mock/test1");
		context.init();
		LeenRequest req = TestUtils.mockRequest(originUri, context);
		LeenResponse res = TestUtils.mockResponse(req);
//		ac = new LeenAsyncContext(req, res, context, timeout, false);
		ac=(LeenAsyncContext) req.startAsync();
		ac.setTimeout(timeout);
	}

	@Test
	public void testFirst() throws Exception {
		ac.postService();
		ac.dispatch(context, "/leen/testRD");
		Thread.sleep(200);
		assertTrue(TestContext.exeMap.get("testRD.executed"));
	}

	// ***********************************计时器测试**********************************

	// 测试计时器的开启与关闭
	// postService调用之后开始计时，进入dispatched状态关闭计时
	// 参考文档 C001.1.2.1、C002.1.2.2
	@Test
	public void testStartAndStopTimeout1() throws Exception {
		Thread.sleep(200);
		assertFalse(ac.isCompleted());
		ac.postService();
		Thread.sleep(200);
		assertTrue(ac.isCompleted());
	}

	// C001.1.2.3
	@Test
	public void testStartAndStopTimeout2() throws Exception {
		ac.postService();
		ac.dispatch(context, "/test1");
		Thread.sleep(200);
		assertTrue(ac.isCompleted());
	}

	// C001.3.2.1
	@Test
	public void testTimeout() throws Exception {
		ac.addListener(new TestAsyncListener());
		ac.addListener(new TestAsyncListner2());
		ac.postService();
		Thread.sleep(200);
		assertTrue(TestUtils.exeMap.get("listener.onTimeout"));
		assertTrue(TestUtils.exeMap.get("listener2.onTimeout"));
		assertTrue(TestUtils.exeMap.get("asyncTimeout.executed"));
		assertTrue(ac.isCompleted());
	}

	// C001.3.2.2
	@Test
	public void testTimeout2() throws Exception {
		ac.addListener(new TestAsyncListner3());
		ac.postService();
		Thread.sleep(200);
		assertNull(TestUtils.exeMap.get("asyncTimeout.executed"));
		assertTrue(ac.isCompleted());
	}

	// **************************startAsync测试 C002.1************************
	// C002.1.1
	@Test
	public void testStartAsync() throws Exception {
		LeenRequest req = getRequest("/leen/testRD");
		// 初始状态测试
		req.startAsync();
		final LeenAsyncContext ac = (LeenAsyncContext) req.getAsyncContext();
		assertNotNull(ac);
		assertTrue(req == ac.getRequest());
		assertTrue(req.getResponse() == ac.getResponse());
		assertTrue(req.isAsyncStarted());
		assertEquals(CommonInfo.DEFAULT_ASYNC_TIMEOUT, ac.getTimeout());
		assertFalse(ac.isUsingCurrentURI());

		// 非初始状态测试
		class MockServlet extends HttpServlet {

			static final String EXECUTE_KEY = "asyncMock.execute";

			@Override
			protected void service(HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {
				req.startAsync();
				assertTrue(ac.isStarted());
				assertFalse(ac.isUsingCurrentURI());
				TestUtils.setExecuted(EXECUTE_KEY);
			}
		}

		context.addServlet("mock", new MockServlet()).addMapping("/asyncMock");
		ac.dispatch("/asyncMock");
		ac.postService();
		Thread.sleep(200);
		assertTrue(TestUtils.isExecuted(MockServlet.EXECUTE_KEY));

		/*
		 * ac.postService(); assertTrue(ac.isStarted()); ac.dispatch();
		 * Thread.sleep(100); req.startAsync();
		 */

	}

	// C002.1.2
	@Test
	public void testStartAsyncWithParams() throws Exception {
		LeenRequest req = getRequest("/leen/testRD");
		HttpServletRequestWrapper reqWrapper = new HttpServletRequestWrapper(
				req);
		HttpServletResponseWrapper respWrapper = new HttpServletResponseWrapper(
				req.getResponse());

		// 初始状态测试
		req.startAsync(reqWrapper, respWrapper);
		LeenAsyncContext ac = (LeenAsyncContext) req.getAsyncContext();
		assertNotNull(ac);
		assertTrue(reqWrapper == ac.getRequest());
		assertTrue(respWrapper == ac.getResponse());
		assertTrue(req.getServletContext() == ac.getContext());
		assertTrue(req.isAsyncStarted());
		assertEquals(CommonInfo.DEFAULT_ASYNC_TIMEOUT, ac.getTimeout());
		assertTrue(ac.isUsingCurrentURI());

		// 非初始状态测试
		class AsyncMock2 extends HttpServlet {
			static final String EXECUTE_KEY = "asyncMock2.executed";
			@Override
			protected void service(HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {
				HttpServletRequestWrapper reqWrapper = new HttpServletRequestWrapper(
						req);
				HttpServletResponseWrapper respWrapper = new HttpServletResponseWrapper(
						resp);
				LeenAsyncContext ac = (LeenAsyncContext) req.startAsync(
						reqWrapper, respWrapper);
				assertEquals(reqWrapper, ac.getRequest());
				assertTrue(respWrapper == ac.getResponse());
				assertTrue(ac.isStarted());
				assertTrue(ac.isUsingCurrentURI());
				TestUtils.setExecuted(EXECUTE_KEY);
			}
		}
		context.addServlet("mockAsync", new AsyncMock2()).addMapping(
				"/asyncMock");
		ac.dispatch("/asyncMock");
		ac.postService();
		Thread.sleep(100);
		assertTrue(TestUtils.isExecuted(AsyncMock2.EXECUTE_KEY));
	}

	private LeenRequest getRequest(String path) throws Exception {
		RequestURI uri = new RequestURI();
		uri.setRawURI(context.getContextPath() + path);
		uri.setContextPath(context.getContextPath());
		LeenRequest req = TestUtils.mockRequest(uri, context);
		TestUtils.mockResponse(req);
		return req;
	}

	// *******************************C002.2******************************

	public void testDispatch() throws Exception {
		// 当AC通过startAsync()启动时：
		LeenRequest req = getRequest("/test");
		LeenResponse resp = TestUtils.mockResponse(req);

		RequestURI currentDispatchURI = TestUtils.MockURI("/test3");
		req.setCurrentDispatchURI(currentDispatchURI);
		LeenAsyncContext ac = (LeenAsyncContext) req.startAsync();
		ac.dispatch();
		ac.postService();
		Thread.sleep(100);
		assertNotNull(TestUtils.exeMap.get(TestServlet3.EXECUTE_KEY));

		// 当AC通过startAsync(request，response)启动时：
		TestUtils.exeMap.remove(TestServlet3.EXECUTE_KEY);

		req = getRequest("/test");
		resp = TestUtils.mockResponse(req);
		RequestURI currentURI = TestUtils.MockURI("/test");
		req.setCurrentDispatchURI(currentDispatchURI);
		req.setforwardAttributes(currentURI);
		ac = (LeenAsyncContext) req.startAsync(req, resp);
		ac.dispatch();
		ac.postService();
		Thread.sleep(10);
		assertNotNull(TestUtils.exeMap.get(TestServlet.EXECUTE_KEY));
	}
}

class TestAsyncListener implements AsyncListener {

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		TestUtils.exeMap.put("listener.onTimeout", true);
	}

	@Override
	public void onError(AsyncEvent event) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
		// TODO Auto-generated method stub
	}

}

class TestAsyncListner2 extends TestAsyncListener {
	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		TestUtils.exeMap.put("listener2.onTimeout", true);
	}
}

class TestAsyncListner3 extends TestAsyncListener {
	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		event.getAsyncContext().complete();
	}
}
