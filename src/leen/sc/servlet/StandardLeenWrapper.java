package leen.sc.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import leen.sc.container.ILeenContext;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;

import org.apache.log4j.Logger;

public class StandardLeenWrapper extends BaseServletWrapper {
	private static Logger log = Logger.getLogger(StandardLeenWrapper.class);
	private HttpServlet servlet;

	public StandardLeenWrapper(String servletName,HttpServlet servlet,ILeenContext context) {
		super(servletName,context);
		log.info("creating wrapper for servlet " + servlet.getClass().getName()
				+ " on context " +getServletContext().getContextPath());
		this.servlet = servlet;

	}

	public void init() throws ServletException {
		setParameters(servlet);
		this.servlet.init(this);
	}

	@Override
	public void leen(LeenRequest request, LeenResponse response)
			throws ServletException, IOException {
		servlet.service(request, response);
	}

	@Override
	public HttpServlet loadServlet() {
		return servlet;
	}

	@Override
	public void destroy() {
		this.servlet.destroy();
	}
	
	public String getClassName(){
		return servlet.getClass().getName();
	}

}
