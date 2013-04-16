package leen.sc.dispatcher;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.container.LeenContext;
import leen.sc.filter.LeenFilterManager;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.LeenResponse;
import leen.sc.util.Retriever;

import org.apache.log4j.Logger;

public class LeenRequestDispatcher implements RequestDispatcher {

	private static Logger log = Logger.getLogger(LeenRequestDispatcher.class);

	private LeenContext context;
	private RequestURI uri;
	private HttpServlet servlet;

	public void setPath(String path) {
		if (context == null)
			throw new IllegalStateException("context must be set");
		if (path == null)
			throw new IllegalArgumentException("uri required");
		if (!(path.equals("") || path.startsWith("/")))
			throw new IllegalArgumentException(
					"uri must start with slash or be empty");
		servlet = null;
		uri = new RequestURI();
		uri.setRawURI(context.getContextPath() + path);
		uri.setContextPath(context.getContextPath());
	}

	public void setServlet(HttpServlet servlet) {
		this.uri = null;
		this.servlet = servlet;
	}

	@Override
	public void forward(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {

		if (!(req instanceof HttpServletRequest)
				|| !(resp instanceof HttpServletResponse))
			throw new IllegalArgumentException(
					"timcat only accpet http request");

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// 验证response是否提交
		if (response.isCommitted())
			throw new IllegalStateException("response already commited");

		if (uri == null && servlet == null)
			throw new IllegalStateException("uri or servletName must be set");

		// request、response向下转型

		LeenRequest lRequest = Retriever.retrieveRequest(request);
		LeenResponse lResponse = Retriever.retrieveResponse(response);

		// 清除缓存
		response.resetBuffer();
		RequestURI originUri = null;

		DispatcherType originDispaterType = lRequest.getDispatcherType();
		// 设置DispatcherType
		lRequest.setDispatcherType(DispatcherType.FORWARD);
		// uri dispatch------------------------------
		if (uri != null) {

			originUri = lRequest.getCurrentURI();

			lRequest.setforwardAttributes(uri);

			context.leen(request, response);

			lRequest.recoverAttributes(originUri);
		}

		// name dispatch--------------------------------
		else {
			LeenFilterManager fm = context.getFilterManager();
			fm.map(servlet, request.getDispatcherType()).doFilter(request,
					response);
		}

		// 恢复dispatcherType
		lRequest.setDispatcherType(originDispaterType);

		// 关闭输出流
		lResponse.close();

	}

	/*
	 * relative uri if (!uri.startsWith("/")) { String path =
	 * lRequest.getRequestURI(); int index = path.lastIndexOf("/"); uri =
	 * path.substring(0, index + 1) + uri; }
	 */

	@Override
	public void include(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {

		if (!(req instanceof HttpServletRequest)
				|| !(resp instanceof HttpServletResponse))
			throw new IllegalArgumentException(
					"timcat only accpet http request");

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		LeenRequest lRequest = Retriever.retrieveRequest(request);
		// 设置DispatcherType
		lRequest.setDispatcherType(DispatcherType.INCLUDE);

		DispatcherType originDispaterType = lRequest.getDispatcherType();

		if (uri != null) {
			RequestURI originURI = lRequest.getCurrentURI();

			lRequest.setIncludeAttributes(uri);

			// query string 入栈
			if (uri.getQueryString() != null)
				lRequest.pushQueryString(uri.getQueryString());

			// 调用容器

			// query string 出栈
			context.leen(request, response);
			lRequest.recoverAttributes(originURI);

		} else {
			LeenFilterManager fm = context.getFilterManager();
			fm.map(servlet, request.getDispatcherType()).doFilter(request,
					response);
		}

		lRequest.setDispatcherType(originDispaterType);
	}

	public void setContext(LeenContext leenContext) {
		this.context = leenContext;
	}

}
