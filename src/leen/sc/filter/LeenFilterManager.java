package leen.sc.filter;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class LeenFilterManager {
	private Logger log = Logger.getLogger(LeenFilterManager.class);

	private List<LeenFilterWrapper> wrapperList = new ArrayList<LeenFilterWrapper>();
	private LinkedList<UrlPatternFilterMappingData> uMappings = new LinkedList<UrlPatternFilterMappingData>();
	private LinkedList<ServletNameFilterMappingData> sMappings = new LinkedList<ServletNameFilterMappingData>();

	public void init() {
		log.debug("initing filterManager,filter count:"+wrapperList.size()+" time "+System.currentTimeMillis());
		
		List<LeenFilterWrapper> toRemove = new ArrayList<LeenFilterWrapper>();
		for (LeenFilterWrapper wrapper : wrapperList) {
			try {
				wrapper.init();
			} catch (ServletException e) {
				toRemove.add(wrapper);
				log.warn("fail to init filter " + wrapper.getFilterName()
						+ ",it will be removed");
			}
		}
		wrapperList.removeAll(toRemove);
	}

	public void destroy() {
		for (LeenFilterWrapper wrapper : wrapperList) {
			wrapper.destroy();
		}
	}

	public LeenFilterChain map(HttpServlet servlet, String url,
			DispatcherType dispatcherType) {
		LeenFilterChain chain = new LeenFilterChain(mapInternal(
				servlet.getServletName(), url, dispatcherType));
		chain.setServlet(servlet);
		return chain;
	}

	public LeenFilterChain map(HttpServlet servlet, DispatcherType dt) {
		if (servlet == null || dt == null)
			throw new IllegalArgumentException();
		List<Filter> filterList = mapByServletName(servlet.getServletName(), dt);
		LeenFilterChain chain = new LeenFilterChain(filterList);
		chain.setServlet(servlet);
		return chain;
	}

	private List<Filter> mapByServletName(String servletName, DispatcherType dt) {
		List<Filter> matchedFilterList = new ArrayList<Filter>();
		for (ServletNameFilterMappingData sMapping : sMappings) {
			if (sMapping.match(servletName, dt)
					&& !matchedFilterList.contains(sMapping.getFilter()))
				matchedFilterList.add(sMapping.getFilter());
		}
		return matchedFilterList;
	}

	public List<Filter> mapInternal(String servletName, String url,
			DispatcherType dispatcherType) {
		List<Filter> matchedFilterList = new ArrayList<Filter>();

		for (UrlPatternFilterMappingData uMapping : uMappings) {
			if (uMapping.match(url, dispatcherType)
					&& !matchedFilterList.contains(uMapping.getFilter())) {
				matchedFilterList.add(uMapping.getFilter());
			}
		}
		matchedFilterList.addAll(mapByServletName(servletName, dispatcherType));
		return matchedFilterList;
	}
	

	public Filter getFilter(String filterName){
		if(filterName==null)
			throw new IllegalArgumentException("filter name required");
		for(LeenFilterWrapper wrapper:wrapperList){
			if(wrapper.getFilterName().equals(filterName))
				return wrapper.getFilter();
		}
		return null;
	} 

	public void setFilterWrapperList(List<LeenFilterWrapper> filterWrapperList) {
		this.wrapperList = filterWrapperList;
	}

	public void addWrapper(LeenFilterWrapper wrapper) {
		if (wrapperList.contains(wrapper))
			return;
		this.wrapperList.add(wrapper);
		log.debug("add filter "+wrapper.getFilterName()+",filter count "+wrapperList.size()+" time "+System.currentTimeMillis());
	}

	public void addSmapping(ServletNameFilterMappingData mapping) {
		if (mapping.isProgramedIn() && !mapping.isMatchAfter())
			sMappings.push(mapping);
		else
			sMappings.add(mapping);
	}

	public void addUmapping(UrlPatternFilterMappingData mapping) {
		if (mapping.isProgramedIn() && !mapping.isMatchAfter())
			uMappings.push(mapping);
		else
			uMappings.add(mapping);
	}

	public List<UrlPatternFilterMappingData> getUmappings() {
		return uMappings;
	}

	public List<ServletNameFilterMappingData> getSmappings() {
		return sMappings;
	}

	public LeenFilterWrapper getWrapper(String filterName) {
		for (LeenFilterWrapper wrapper : wrapperList)
			if (wrapper.getFilterName().equals(filterName))
				return wrapper;
		return null;
	}
}
