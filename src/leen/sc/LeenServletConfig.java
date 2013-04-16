package leen.sc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import leen.sc.container.ILeenContext;

import org.apache.log4j.Logger;

public class LeenServletConfig implements ServletConfig {
	private static Logger log=Logger.getLogger(LeenServletConfig.class);
	
	public final static int TYPE1=1;
	public final static int TYPE2=2;
	public final static int TYPE3=3;
	
	private String servletName;
	private String servletClass;
	private String urlPattern;
	private int urlPatternType;
	private Map<String, String> initParameters = new HashMap<String, String>();
	private ILeenContext context;

	public LeenServletConfig(String servletName, String urlPattern,
			String servletClass, Map<String, String> initParameters,
			ILeenContext context) {
		setUrlPattern(urlPattern);
		if(servletClass==null||servletName==null||initParameters==null||context==null)
			throw new IllegalArgumentException();
		this.servletName = servletName;
		this.servletClass = servletClass;
		this.initParameters = initParameters;
		this.context = context;
	}

	private void setUrlPattern(String urlPattern){
		if(urlPattern==null)
			throw new IllegalArgumentException("urlpattern required");
		int idx1=urlPattern.indexOf('*');
		if(idx1==-1)
			this.urlPatternType=TYPE1;
		else if(urlPattern.indexOf("*", idx1+1)!=-1)
			throw new IllegalArgumentException("invalid urlpattern,multi '*' contained");
		else if(urlPattern.endsWith("*"))
			this.urlPatternType=TYPE2;
		else if(urlPattern.startsWith("*."))
			this.urlPatternType=TYPE3;
		this.urlPattern=urlPattern;
			
	}
	
	@Override
	public String getServletName() {
		return servletName;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new Enumeration<String>() {
			private Iterator<String> it = initParameters.keySet().iterator();

			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}

			@Override
			public String nextElement() {
				return it.next();
			}
		};
	}

	public String getServletClass() {
		return servletClass;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public int getUrlPatternType(){
		return urlPatternType;
	}
	
}
