package leen.sc.servlet;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;

import leen.sc.container.ILeenContext;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;
import leen.sc.util.ResponseMessages;

import org.apache.log4j.Logger;

public class SingleThreadLeenWrapper extends BaseServletWrapper {
	private Logger log = Logger.getLogger(SingleThreadLeenWrapper.class);

	private LinkedList<HttpServlet> servletStack;
	private Class<?> servletClass;
	private int servletCount;
	private int maxServletCount;
	

	public SingleThreadLeenWrapper(String servletName,Class<?> servletClass,ILeenContext context) {
		super(servletName,context);
		log.info("creating SingleThreadLeenWrapper,servlet: "
				+ servletClass.getName());
		if (!HttpServlet.class.isAssignableFrom(servletClass)
				|| !SingleThreadModel.class.isAssignableFrom(servletClass))
			throw new IllegalArgumentException(
					"servletClass must be subclass of httpServlet and it must implement Container,SingleThreadModel");
		this.servletClass = servletClass;

	}
	
	@Override
	public void init() throws ServletException {
	}

	@Override
	public void leen(LeenRequest request, LeenResponse response)
			throws ServletException, IOException {
		try {
			HttpServlet servlet = null;
			synchronized (servletStack) {
				if (servletStack.isEmpty()) {
					if (servletCount < maxServletCount) {
						servlet = (HttpServlet) servletClass.newInstance();
						servlet.init(this);
					}
				} else
					servlet = servletStack.pop();

			}
			if (servlet == null)
				this.wait();
			servlet.service(request, response);
			synchronized (servletStack) {
				servletStack.push(servlet);
				this.notify();
			}
		} catch (InstantiationException e) {
			response.setStatus(500);
			response.getWriter()
					.write(ResponseMessages.exception(e.getCause()));
			log.warn(servletClass.getName() + " can not be instantiated");
		} catch (IllegalAccessException e) {
			log.warn(servletClass.getName() + " can not be accessed");
			response.setStatus(500);
			response.getWriter()
					.write(ResponseMessages.exception(e.getCause()));
		} catch (InterruptedException e) {
		}
	}

	@Override
	public HttpServlet loadServlet() throws ServletException{
		try {
			HttpServlet servlet = null;
			synchronized (servletStack) {
				if (servletStack.isEmpty()) {
					if (servletCount < maxServletCount) {
						servlet = (HttpServlet) servletClass.newInstance();
						setParameters(servlet);
						servlet.init(this);
					}
				} else
					servlet = servletStack.pop();
			}
			return servlet;
		} catch (InstantiationException e) {
			log.warn("servlet "+servletClass.getName()+" can't be instantiated");
			throw new IllegalStateException();
		}
		catch(IllegalAccessException e){
			log.warn("servlet "+servletClass.getName()+" can't be accessed");
			throw new IllegalStateException();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		for (HttpServlet servlet : servletStack) {
			servlet.destroy();
		}
	}
	
	public String getClassName(){
		return this.servletClass.getName();
	}

}
