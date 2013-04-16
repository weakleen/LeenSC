package leen.sc.test.servlet;

import static leen.sc.test.real.TestDispatcher.NEW_VALUES;
import static leen.sc.test.real.TestDispatcher.PARAM_NAMES;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.test.real.util.TestUtils;

public class TestQueryStringServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// ≤‚ ‘Query string
		boolean qRS = true;
		for (int i = 0; i < PARAM_NAMES.length; i++) {
			System.out.println("expected:"+NEW_VALUES[i]+",real:"+req.getParameter(PARAM_NAMES[i]));
			qRS = qRS && req.getParameter(PARAM_NAMES[i]).equals(NEW_VALUES[i]);
		}
		if (qRS)
			TestUtils.setExecuted("querystring");
	}
}
