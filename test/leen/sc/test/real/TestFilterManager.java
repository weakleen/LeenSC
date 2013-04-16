package leen.sc.test.real;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import junit.framework.TestCase;
import leen.sc.container.LeenContext;
import leen.sc.filter.LeenFilterManager;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.LeenResponse;
import leen.sc.test.filter.TestInitParamsFilter;
import leen.sc.test.real.util.MockContextFactory;
import leen.sc.test.real.util.TestUtils;

import org.junit.Before;

public class TestFilterManager extends TestCase {

	private LeenFilterManager filterManager;
	private LeenContext context;
	private String url="/testFilterServlet";
	private String servletName="testFilterOrderServlet";
	EnumSet<DispatcherType> dts=EnumSet.of(DispatcherType.REQUEST);
	private LeenRequest req;
	private LeenResponse resp;
	@Before
	public void setUp() throws Exception {
		filterManager = new LeenFilterManager();
		context = MockContextFactory.getInstance();
		context.setFilterManager(filterManager);
		context.init();
		class TestFilterOrderServlet extends HttpServlet{
			private static final long serialVersionUID = 4034258537803067326L;
		}
		context.addServlet(servletName, new TestFilterOrderServlet()).addMapping(url);
		
		RequestURI uri=new RequestURI();
		uri.setRawURI("/Mock"+url);
		uri.setContextPath("/Mock");
		 req=TestUtils.mockRequest(uri, context);
		 resp=TestUtils.mockResponse(req);
	}

	int flag = 0;

	// 测试filterChain中filter的顺序符合规范
	public void testFilterChianOrder() throws Exception{
		
		class Filter1 extends TestFilterBase{
			protected void process(ServletRequest request,
					ServletResponse response) {
					assertEquals(3,++flag);
			}
		}
		class Filter2 extends TestFilterBase{
			protected void process(ServletRequest request,
					ServletResponse response) {
				assertEquals(1,++flag);
			}
		}
		class Filter3 extends TestFilterBase{
			protected void process(ServletRequest request,
					ServletResponse response) {
				assertEquals(2,++flag);
			}
		}

		
		context.addFilter("filter1",new Filter1()).addMappingForServletNames(dts, true, servletName);
		context.addFilter("filter2",new Filter2()).addMappingForUrlPatterns(dts	, true, url);
		context.addFilter("filter3",new Filter3()).addMappingForUrlPatterns(dts	, true, url);
		context.request(req, resp);
	}

//	测试named-dispatcher filter
	public void testNamedDispatcherFilter() throws Exception{
		final String TEST_NAME_FILTER_EXE_KEY="TestNamedFilter.executed";
		final String TEST_NAME_FILTER2_EXE_KEY="TestNamedFilter2.executed";
		class TestNamedFilter extends TestFilterBase{
			@Override
			protected void process(ServletRequest request,
					ServletResponse response) {
				TestUtils.setExecuted(TEST_NAME_FILTER_EXE_KEY);
			}
		}
		
		class TestNamedFilter2 extends TestFilterBase{
			@Override
			protected void process(ServletRequest request,
					ServletResponse response) {
				TestUtils.setExecuted(TEST_NAME_FILTER2_EXE_KEY);
			}
		}
		
		context.addFilter("testNamedFilter", new TestNamedFilter()).addMappingForServletNames(dts, true, servletName);
		context.addFilter("testNamedFilter2", new TestNamedFilter2()).addMappingForServletNames(dts, true, servletName);
		HttpServlet servlet=(HttpServlet) context.getServlet(servletName);
		filterManager.map(servlet,DispatcherType.REQUEST).doFilter(req, resp);
		assertTrue(TestUtils.isExecuted(TEST_NAME_FILTER_EXE_KEY));
		assertTrue(TestUtils.isExecuted(TEST_NAME_FILTER2_EXE_KEY));
	}
	
//	测试init方法是否被调用，init-params是否正确设置
	public void testInit(){
		TestInitParamsFilter filter= (TestInitParamsFilter) context.getFilterManager().getFilter("testInitParams");
		assertNotNull(filter);
		assertTrue(TestUtils.isExecuted(TestInitParamsFilter.EXE_KEY));
		assertNotNull(filter.getParam1());
		assertNotNull(filter.getParam2());
	}
}

abstract class TestFilterBase implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		process(request, response);
		chain.doFilter(request, response);
	}

	protected abstract void process(ServletRequest request,
			ServletResponse response);

	@Override
	public void destroy() {
	}

}
