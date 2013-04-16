package leen.sc.test.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.test.formal.TestContext;

public class TestWrapperServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		TestContext.exeMap.put("testWrapperExe", true);
		System.out.println("req:"+req+",resp:"+resp);
		req.getParameter("leen");resp.getWriter();
	}
}
