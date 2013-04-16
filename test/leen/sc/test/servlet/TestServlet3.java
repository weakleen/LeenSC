package leen.sc.test.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.test.real.util.TestUtils;

public class TestServlet3 extends HttpServlet {
	
	public static final String EXECUTE_KEY="test3.executed";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		TestUtils.exeMap.put(EXECUTE_KEY, true);
	}
}
