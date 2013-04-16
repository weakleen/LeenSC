package leen.sc.servlet;

import java.beans.PropertyDescriptor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import leen.sc.container.ILeenContext;
import leen.sc.container.sub.UrlPattern;
import leen.sc.util.CollectionEnumeration;

public abstract class BaseServletWrapper implements ServletWrapper {
	private static Logger log=Logger.getLogger(BaseServletWrapper.class);
	private ILeenContext context;
	private String servletName;
	private Map<String,String> initParameters=new HashMap<String,String>();
	private UrlPattern urlPattern;
	
	protected BaseServletWrapper(String servletName,ILeenContext context){
		this.servletName=servletName;
		this.context=context;
	}
	
	protected void setParameters(HttpServlet servlet) {
		for(String property:initParameters.keySet()){
			try {
				PropertyDescriptor pd=new PropertyDescriptor(property, servlet.getClass());
				pd.getWriteMethod().invoke(servlet, initParameters.get(property));
			} catch (Exception e) {
				log.warn("fail to set property '"+property+"' for servlet '"+servlet.getClass().getName()+"'");
			}
		}
	}
	
	@Override
	public String getServletName() {
		return this.servletName;
	}

	@Override
	public ILeenContext getServletContext() {
		return context;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new CollectionEnumeration<String>(initParameters.keySet());
	}

	@Override
	public void setServletName(String servletName) {
		this.servletName=servletName;
	}

	@Override
	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters=initParameters;
	}

	@Override
	public void setInitParameter(String name, String value) {
		this.initParameters.put(name, value);
	}
	
	@Override
	public Map<String, String> getInitParameters() {
		return initParameters;
	}
	
	@Override
	public void destroy() {
		initParameters.clear();
	}
}
