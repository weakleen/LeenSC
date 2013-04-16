package leen.sc.container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.SingleThreadModel;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.ConfigException;
import leen.sc.ServletMapExcepion;
import leen.sc.classloader.LeenCLFactory;
import leen.sc.container.sub.LeenServletRegistration;
import leen.sc.dispatcher.LeenRequestDispatcher;
import leen.sc.filter.LeenFilterChain;
import leen.sc.filter.LeenFilterManager;
import leen.sc.filter.LeenFilterWrapper;
import leen.sc.filter.ServletNameFilterMappingData;
import leen.sc.filter.UrlPatternFilterMappingData;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;
import leen.sc.servlet.ServletWrapper;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.servlet.SingleThreadLeenWrapper;
import leen.sc.servlet.StandardLeenWrapper;
import leen.sc.util.ResponseMessages;
import leen.sc.util.Retriever;

import org.apache.log4j.Logger;

public class LeenContext implements ILeenContext {
	private static final Logger log = Logger.getLogger(LeenContext.class);
	private String contextPath;
	private String realPath;
	private WebXmlParser webXmlParser = new WebXmlParser();
	private ClassLoader loader;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private List<ServletContextListener> listenerList = new ArrayList<ServletContextListener>();

	private ServletWrapperManager wrapperMgr;;
	private LeenFilterManager filterManager;
	private WelcomeManager welcomeManager;

	private Map<String, FilterRegistration.Dynamic> filterRegs = new HashMap<String, FilterRegistration.Dynamic>();
	private Map<String, LeenServletRegistration> sRegs = new HashMap<String, LeenServletRegistration>();
	private String enginePath;

	private Executor executor;

	public LeenContext(String enginePath, String contextPath) {
		this.enginePath = enginePath;
		this.contextPath = contextPath;
		if (contextPath.equals("/"))
			realPath = enginePath + File.separator + "ROOT";
		else
			realPath = enginePath + File.separator + contextPath.substring(1);
		log.debug("real path of context " + contextPath + " is " + realPath);
		initClassLoader();
	}

	public void init() throws ConfigException {
		log.info("creating context " + contextPath);
		config();
		wrapperMgr.init();
		filterManager.init();
		for (ServletContextListener listener : listenerList) {
			ServletContextEvent sce = new ServletContextEvent(this);
			listener.contextInitialized(sce);
		}
	}

	private void initClassLoader() {
		loader = LeenCLFactory.getContext(realPath);
	}

	private void config() throws ConfigException {
		webXmlParser.parse(this, new File(realPath + File.separator + "WEB-INF"
				+ File.separator + "web.xml"));
	}

	private FilterChain map(LeenRequest request, DispatcherType dispatcherType)
			throws ServletMapExcepion, ServletException {
		HttpServlet servlet = wrapperMgr.map(request);
		if (servlet == null) {
			log.debug("URI " + request.getRequestURI() + " not mapped");
			throw new ServletMapExcepion();
		}
		log.debug("URI " + request.getRequestURI() + " mapped to "
				+ servlet.getClass().getName());
		LeenFilterChain filterChain = filterManager.map(servlet,
				request.getSubUrlForMapping(), dispatcherType);
		return filterChain;
	}

	public void request(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LeenRequest lRequest = Retriever.retrieveRequest(request);
		lRequest.setDispatcherType(DispatcherType.REQUEST);
		lRequest.setCurrentDispatchURI(lRequest.getCurrentURI());
		leen(request, response);
	}

	@Override
	public void leen(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		log.debug("get request " + req.getRequestURI() + " in context "
				+ contextPath);
		LeenRequest request = Retriever.retrieveRequest(req);
		LeenResponse response = Retriever.retrieveResponse(res);
		Thread.currentThread().setContextClassLoader(loader);
		request.setContext(this);
		try {
			map(request, request.getDispatcherType()).doFilter(
					request.getWrapper(), response.getWrapper());
			if (request.getDispatcherType() == DispatcherType.ASYNC)
				request.getAsyncContext().postService();
		}
		/*
		 * catch (Throwable e) { e.printStackTrace(); }
		 */catch (ServletMapExcepion e) {
			try {
				welcomeManager.welcome(request, response);
			} catch (ServletMapExcepion e1) {
				HttpServlet defaultServlet = wrapperMgr.getDefault();
				if (defaultServlet != null) {
					log.debug("default servlet service");
					defaultServlet.service(request.getWrapper(),
							response.getWrapper());
				} else {
					response.setStatus(404);
					response.getWriter().write(
							ResponseMessages.notFound(request.getRequestURI()));
					response.flushBuffer();
				}
			}
		}

	}

	public ServletWrapperManager getServletManager() {
		return wrapperMgr;
	}

	public LeenFilterManager getFilterManager() {
		return filterManager;
	}

	public HttpServlet getServletInternal(String name) throws ServletException {
		if (name == null)
			throw new IllegalArgumentException("servlet name required");
		return wrapperMgr.getServlet(name);
	}

	@Override
	public void setFilterWrapperList(List<LeenFilterWrapper> filterWrapperList) {
		filterManager.setFilterWrapperList(filterWrapperList);
	}

	@Override
	public void setFilterManager(LeenFilterManager filterMgr) {
		this.filterManager = filterMgr;
	}

	public WelcomeManager getWelcomeManager() {
		return welcomeManager;
	}

	public void setWelcomeManager(WelcomeManager welcomeManager) {
		this.welcomeManager = welcomeManager;
		welcomeManager.setContext(this);
		welcomeManager.setSWM(wrapperMgr);
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public ServletWrapperManager getSWM() {
		return wrapperMgr;
	}

	public void setSWM(ServletWrapperManager swm) {
		this.wrapperMgr = swm;
	}

	// Servlet Api Implementation

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public void addWrapper(ServletWrapper wrapper) {
	}

	@Override
	public ServletContext getContext(String uripath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMimeType(String file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return loader.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return loader.getResourceAsStream(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		LeenRequestDispatcher lRd = new LeenRequestDispatcher();
		lRd.setContext(this);
		lRd.setPath(path);
		return lRd;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		HttpServlet servlet = null;
		try {
			servlet = wrapperMgr.getServlet(name);
		} catch (ServletException e) {
			return null;
		}
		if (servlet == null)
			return null;
		LeenRequestDispatcher lRd = new LeenRequestDispatcher();
		lRd.setContext(this);
		lRd.setServlet(servlet);
		return lRd;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return wrapperMgr.getServlet(name);
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return wrapperMgr.getServlets();
	}

	@Override
	public Enumeration<String> getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String msg) {
		log.info(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		log.warn(msg + "\n" + exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		log.warn(message + "\n" + throwable);
	}

	@Override
	public String getRealPath(String path) {
		if (path == null)
			throw new IllegalArgumentException();
		log.debug("getting real path for " + path);
		path = path.replaceFirst("^[/\\\\]+", "");
		return realPath + File.separator + path;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		Enumeration<String> attributeNames = new Enumeration<String>() {
			private Iterator<String> it = attributes.keySet().iterator();

			@Override
			public String nextElement() {
				return it.next();
			}

			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}
		};
		return attributeNames;
	}

	@Override
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		try {
			Class<HttpServlet> servletClass = (Class<HttpServlet>) loader
					.loadClass(className);
			return addServlet(servletName, servletClass);
		} catch (ClassNotFoundException e) {
			log.warn("servlet " + className + " not found");
			return null;
		} catch (NoClassDefFoundError e) {
			log.warn("class not def " + className);
			return null;
		}
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		if (servlet instanceof SingleThreadModel
				|| !(servlet instanceof HttpServlet))
			throw new IllegalArgumentException();
		if (wrapperMgr.getWrapper(servletName) != null)
			return null;
		ServletWrapper wrapper = new StandardLeenWrapper(servletName,
				(HttpServlet) servlet, this);
		try {
			wrapperMgr.addWrapper(wrapper);
			return getServletRegistration(servletName);
		} catch (ServletException e) {
			log.warn("fail to add servlet " + servlet.getClass().getName());
			return null;
		}
	}

	@Override
	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		try {
			if (wrapperMgr.getServlet(servletName) != null)
				return null;
			if (SingleThreadModel.class.isAssignableFrom(servletClass)) {
				ServletWrapper wrapper = new SingleThreadLeenWrapper(
						servletName, servletClass, this);
				wrapperMgr.addWrapper(wrapper);
				return getServletRegistration(servletName);
			} else {
				HttpServlet servlet = (HttpServlet) createServlet(servletClass);
				return addServlet(servletName, servlet);
			}
		} catch (ServletException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> c)
			throws ServletException {
		try {
			HttpServlet servlet = (HttpServlet) c.newInstance();
			return (T) servlet;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException();
		}
	}

	@Override
	public ServletRegistration.Dynamic getServletRegistration(String servletName) {
		if (sRegs.containsKey(servletName))
			return sRegs.get(servletName);
		ServletWrapper wrapper = wrapperMgr.getWrapper(servletName);
		if (wrapper == null)
			return null;
		LeenServletRegistration reg = new LeenServletRegistration(wrapper,
				wrapperMgr);
		sRegs.put(servletName, reg);
		return reg;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return sRegs;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		try {
			return addFilter(filterName,
					(Class<Filter>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		LeenFilterWrapper wrapper = new LeenFilterWrapper(filterName, filter,
				this);
		filterManager.addWrapper(wrapper);
		return getFilterRegistration(filterName);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		return addFilter(filterName, createFilter(filterClass));
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> c) {
		try {
			T filter = c.newInstance();
			return filter;
		} catch (InstantiationException e) {
			throw new IllegalArgumentException();
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public FilterRegistration.Dynamic getFilterRegistration(String filterName) {
		if (filterManager.getWrapper(filterName) == null)
			return null;
		if (filterRegs.containsKey(filterName))
			return filterRegs.get(filterName);
		LeenDynamic dm = new LeenDynamic(filterManager.getWrapper(filterName));
		filterRegs.put(filterName, dm);
		return dm;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return filterRegs;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes)
			throws IllegalStateException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		try {
			addListener(listenerClass.newInstance());
		} catch (InstantiationException e) {
			log.warn("adding listener error," + listenerClass.getName()
					+ " can't be instantiated");
		} catch (IllegalAccessException e) {
			log.warn("adding listener error," + listenerClass.getName()
					+ " can't be accessed");
		}
	}

	@Override
	public void addListener(String className) {
		try {
			Class<EventListener> listenerClass = (Class<EventListener>) Class
					.forName(className);
			addListener(listenerClass);
		} catch (ClassNotFoundException e) {
			log.warn("adding listener error," + className + " not found");
		}

	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		if (t instanceof ServletContextListener) {
			ServletContextListener ctxListener = (ServletContextListener) t;
			listenerList.add(ctxListener);
			log.debug("listener " + ctxListener.getClass() + " added");
		}
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> c)
			throws ServletException {
		try {
			T listener = c.newInstance();
			addListener(listener);
			return listener;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException();
		}

	}

	@Override
	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassLoader getClassLoader() {
		return loader;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	class LeenDynamic implements FilterRegistration.Dynamic {
		private LeenFilterWrapper wrapper;

		public LeenDynamic(LeenFilterWrapper wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public void addMappingForServletNames(
				EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
				String... servletNames) {
			for (String servletName : servletNames)
				filterManager.addSmapping(new ServletNameFilterMappingData(
						wrapper.getFilter(), servletName, dispatcherTypes,
						true, isMatchAfter));
		}

		@Override
		public Collection<String> getServletNameMappings() {
			List<String> servletNames = new ArrayList<String>();
			for (ServletNameFilterMappingData smapping : filterManager
					.getSmappings())
				servletNames.add(smapping.getServletName());
			return servletNames;
		}

		@Override
		public void addMappingForUrlPatterns(
				EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
				String... urlPatterns) {
			for (String urlPattern : urlPatterns)
				filterManager.addUmapping(new UrlPatternFilterMappingData(
						wrapper.getFilter(), urlPattern, dispatcherTypes, true,
						isMatchAfter));
		}

		@Override
		public Collection<String> getUrlPatternMappings() {
			List<String> servletNames = new ArrayList<String>();
			for (ServletNameFilterMappingData smapping : filterManager
					.getSmappings())
				servletNames.add(smapping.getServletName());
			return servletNames;
		}

		@Override
		public String getName() {
			return wrapper.getFilterName();
		}

		@Override
		public String getClassName() {
			return wrapper.getFilterClass();
		}

		@Override
		public boolean setInitParameter(String name, String value) {
			if (wrapper.existParameter(name))
				return false;
			wrapper.addInitParameter(name, value);
			return true;
		}

		@Override
		public String getInitParameter(String name) {
			return wrapper.getInitParameter(name);
		}

		@Override
		public Set<String> setInitParameters(Map<String, String> initParameters) {
			wrapper.setInitParameters(initParameters);
			return initParameters.keySet();
		}

		@Override
		public Map<String, String> getInitParameters() {
			return wrapper.getInitParameters();
		}

		@Override
		public void setAsyncSupported(boolean isAsyncSupported) {
			// TODO Auto-generated method stub
		}

	}

}
