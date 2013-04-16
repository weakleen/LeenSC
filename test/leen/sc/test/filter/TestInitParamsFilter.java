package leen.sc.test.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import leen.sc.test.real.util.TestUtils;

public class TestInitParamsFilter implements Filter {
	
	public static final String EXE_KEY="TestInitParamsFilter.executed";
	
	private String param1;
	private String param2;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		TestUtils.setExecuted(EXE_KEY);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

	}

	@Override
	public void destroy() {
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}
	
	

}
