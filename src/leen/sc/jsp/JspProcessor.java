package leen.sc.jsp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.container.LeenContext;
import leen.sc.request.LeenRequest;
import leen.sc.util.Retriever;

public class JspProcessor extends HttpServlet {
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LeenRequest request =null;
		if(req instanceof LeenRequest)
			request=(LeenRequest) req;
		else
		request =Retriever.retrieveRequest(req);
		
		String subURI=request.getSubUrlForMapping();
		if(!subURI.endsWith(".jsp"))
			throw new RuntimeException("");
		String jspName=subURI.substring(0, subURI.length()-4);
		String servletName=jspName.replace("/", "_")+"_jsp";
		LeenContext context=(LeenContext) req.getServletContext();
		if(context.getServlet(servletName)==null)
		{
			String servletClassName="org.apache.jsp"+jspName.replace("/", ".")+"_jsp";
			ServletRegistration reg=context.addServlet(servletName, servletClassName);
			if(reg==null)
				throw new ServletException("fail to register servlet for jsp "+jspName);
		}
		context.getServlet(servletName).service(req, resp);
	}
}
