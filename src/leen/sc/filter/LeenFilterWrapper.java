package leen.sc.filter;

import java.beans.PropertyDescriptor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import leen.sc.container.ILeenContext;

import org.apache.log4j.Logger;

public class LeenFilterWrapper implements FilterConfig {
	private Logger log = Logger.getLogger(LeenFilterWrapper.class);

	private String filterName;
	private Map<String, String> initParameters=new HashMap<String,String>();
	private Filter filter;
	private ILeenContext context;

	public LeenFilterWrapper(String filterName, Filter filter,
			ILeenContext context) {
		this.filter = filter;
		this.filterName = filterName;
		this.context = context;
	}

	public void init() throws ServletException {
		log.debug("init filter wrapper '"+filterName+"'");
		for(String property:initParameters.keySet()){
			try {
				PropertyDescriptor pd=new PropertyDescriptor(property, filter.getClass());
				pd.getWriteMethod().invoke(filter, initParameters.get(property));
			} catch (Exception e) {
				log.warn("fail to set property '"+property+"' for filter '"+filter.getClass().getName()+"'");
			}
		}
		filter.init(this);
	}

	public void destroy() {
		filter.destroy();
	}

	public Filter getFilter() {
		return this.filter;
	}

	@Override
	public String getFilterName() {
		return filterName;
	}

	public String getFilterClass() {
		return filter.getClass().getName();
	}

	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}

	@Override
	public ILeenContext getServletContext() {
		return context;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	public void addInitParameter(String name, String value) {
		initParameters.put(name, value);
	}

	public boolean existParameter(String name) {
		if (initParameters.containsKey(name))
			return true;
		return false;
	}

	public Map<String, String> getInitParameters() {
		return initParameters;
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

}
