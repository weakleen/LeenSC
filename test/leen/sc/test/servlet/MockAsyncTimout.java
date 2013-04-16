package leen.sc.test.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.test.real.util.TestUtils;

public class MockAsyncTimout extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		TestUtils.exeMap.put("asyncTimeout.executed", true);
	}
}
