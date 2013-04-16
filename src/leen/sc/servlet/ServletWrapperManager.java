package leen.sc.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import leen.sc.container.sub.SUrlPattern;
import leen.sc.container.sub.UrlPatternType;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.startup.util.CommonInfo;

import org.apache.log4j.Logger;

public class ServletWrapperManager {
	private Logger log = Logger.getLogger(ServletWrapperManager.class);

	private Enumeration<Servlet> servletEnum;
	private List<SUrlPattern> sUrlPatterns = new ArrayList<SUrlPattern>();
	private ServletWrapper defaultWrapper;
	private Map<String, ServletWrapper> wrapperMap = new HashMap<String, ServletWrapper>();
	private boolean hasRootUrlPattern;
	private boolean hasDefaultUrlPattern;

	public void init() {
		for (ServletWrapper wrapper : wrapperMap.values()) {
			try {
				wrapper.init();
			} catch (ServletException e) {
				log.warn("fail to init servlet " + wrapper.getServletName()
						+ " it will be removed from the container");
				removeWrapper(wrapper);
			}
		}
	}

	public HttpServlet map(LeenRequest request) throws ServletException {

		for (SUrlPattern pattern : sUrlPatterns) {
			if (pattern.getType() == UrlPatternType.DEFAULT)
				continue;
			String matched = pattern.match(request.getSubUrlForMapping());
			if (matched != null) {
				switch(request.getDispatcherType()){
				case REQUEST:request.setServletPath(matched);break;
				case FORWARD:request.setServletPath(matched);break;
				}
				return pattern.getWrapper().loadServlet();
			}
		}
		return null;
	}

	public boolean exist(String subUrl) {
		for (SUrlPattern pattern : sUrlPatterns) {
			if (pattern.getType() == UrlPatternType.DEFAULT)
				continue;
			String matched = pattern.match(subUrl);
			if (matched != null) {
				return true;
			}
		}
		return false;
	}

	public HttpServlet getServlet(String name) throws ServletException {
		if (wrapperMap.get(name) == null)
			return null;
		return wrapperMap.get(name).loadServlet();
	}

	public Enumeration<Servlet> getServlets() {
		return servletEnum;
	}

	public void addWrapper(ServletWrapper wrapper) throws ServletException {
		wrapper.init();
		wrapperMap.put(wrapper.getServletName(), wrapper);
	}

	public void removeWrapper(ServletWrapper wrapper) {
		wrapperMap.remove(wrapper.getServletName());
		for (SUrlPattern pattern : sUrlPatterns) {
			if (pattern.getWrapper() == wrapper)
				sUrlPatterns.remove(pattern);
		}
	}

	public boolean addSUrlPattern(SUrlPattern sUrlPattern) {
		if (hasDefaultUrlPattern
				&& sUrlPattern.getType() == UrlPatternType.DEFAULT)
			return false;
		if (hasRootUrlPattern && sUrlPattern.getType() == UrlPatternType.ROOT)
			return false;
		if (sUrlPatterns.contains(sUrlPattern))
			return false;
		if (sUrlPattern.getType() == UrlPatternType.DEFAULT) {
			hasDefaultUrlPattern = true;
			defaultWrapper = sUrlPattern.getWrapper();
		}
		if (sUrlPattern.getType() == UrlPatternType.ROOT)
			hasRootUrlPattern = true;
		sUrlPatterns.add(sUrlPattern);
		Collections.sort(sUrlPatterns);
		
		log.debug("pattern " + sUrlPattern.getUrlPattern()+" added,pattern length "+sUrlPatterns.size());
		return true;
	}

	public ServletWrapper getWrapper(String servletName) {
		return wrapperMap.get(servletName);
	}

	public HttpServlet getDefault() throws ServletException {
		if (defaultWrapper == null)
			return null;
		return defaultWrapper.loadServlet();
	}

}
