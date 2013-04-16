package leen.sc.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import leen.sc.InetInfo;
import leen.sc.RequestException;
import leen.sc.container.ILeenContext;
import leen.sc.dispatcher.LeenAsyncContext;
import leen.sc.response.LeenResponse;
import leen.sc.session.SessionManager;
import leen.sc.startup.util.CommonInfo;
import leen.sc.util.CollectionEnumeration;

import org.apache.log4j.Logger;

public class LeenRequest implements HttpServletRequest {

	private static Logger log = Logger.getLogger(LeenRequest.class);

	private static Pattern headerPattern = Pattern.compile("(\\S+): (.*)");
	private static Pattern nextLineValuePattern = Pattern
			.compile("( |\t)+(.*)");

	private SessionManager sessionManager;
	private LeenResponse response;
	private LeenServletInputStream servletInput;
	private BufferedReader servletReader;

	// K002.3.1
	// ******************************************************************
	private RequestURI currentURI = new RequestURI();
	private RequestURI currentDispatchURI = currentURI;
	private RequestURI lastDispatchURI;
	private RequestURI facadeURI = currentURI;// path elements访问该属性

	protected void setFacadeURI(RequestURI facadeURI) {
		this.facadeURI = facadeURI;
	}

	private static final String HOST_HEADER_NAME = "host";

	public RequestURI getCurrentURI() {
		return currentURI;
	}

	public void setCurrentURI(RequestURI uri) {
		if (uri == null)
			throw new IllegalArgumentException();
		this.currentURI = uri;
	}

	public String getCurrentDispatchURI() {
		return currentDispatchURI.getURI();
	}

	public void setCurrentDispatchURI(RequestURI currentDispatchURI) {
		if (currentDispatchURI == null)
			throw new IllegalArgumentException();
		this.currentDispatchURI = currentDispatchURI;
	}

	public void setLastDispatchURI(RequestURI lastDispatchURI) {
		if (lastDispatchURI == null)
			throw new IllegalArgumentException();
		this.lastDispatchURI = lastDispatchURI;
	}

	public void setforwardAttributes(RequestURI nextURI) {
		if (nextURI == null)
			throw new IllegalArgumentException("nextURI required");
		currentURI = nextURI;
		facadeURI = currentURI;
		if (nextURI.getQueryString() != null)
			parameters.push(nextURI.getQueryString());
	}

	public void recoverAttributes(RequestURI lastURI) {
		if (currentURI.getQueryString() != null)
			parameters.pop();
		currentURI = lastURI;
		setFacadeURI();
	}

	public void setIncludeAttributes(RequestURI nextURI) {
		if (nextURI == null)
			throw new IllegalArgumentException("nextURI required");
		currentURI = nextURI;
		facadeURI = currentDispatchURI;
		if (nextURI.getQueryString() != null)
			parameters.push(nextURI.getQueryString());
	}

	public void setAsyncAttributes(RequestURI nextDipatchURI) {

		if (nextDipatchURI == null)
			throw new IllegalArgumentException("nextDispatchURI required");

		if (lastDispatchURI == null) {
			baseAttributeNames.add(AsyncContext.ASYNC_CONTEXT_PATH);
			baseAttributeNames.add(AsyncContext.ASYNC_PATH_INFO);
			baseAttributeNames.add(AsyncContext.ASYNC_QUERY_STRING);
			baseAttributeNames.add(AsyncContext.ASYNC_REQUEST_URI);
			baseAttributeNames.add(AsyncContext.ASYNC_SERVLET_PATH);
		}

		lastDispatchURI = currentDispatchURI;
		currentDispatchURI = nextDipatchURI;
		currentURI = currentDispatchURI;
		facadeURI = currentURI;
		parameters.clear();
	}

	private void setFacadeURI() {
		if (dispatcherType == DispatcherType.INCLUDE)
			facadeURI = currentDispatchURI;
		else
			facadeURI = currentURI;
	}

	// ***********************************************************************
	private ContentType contentType = new ContentType();
	private int contentLength;
	private ServerInfo serverInfo;
	private String encoding = System.getProperty("file.encoding");

	private String protocol;
	private String method;
	private Parameters parameters = new Parameters();

	protected Parameters getParameters() {
		return parameters;
	}

	private Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Cookie[] cookies;
	private String sessionId;
	private ILeenContext context;

	private DispatcherType dispatcherType = DispatcherType.REQUEST;
	private boolean useInputStream = false;
	private boolean useReader = false;

	private boolean parameterParsed;

	private ThreadLocal<HttpServletRequestWrapper> wrapperThreadLocal = new ThreadLocal<HttpServletRequestWrapper>();

	private String host;

	public LeenRequest(InputStream input) {
		servletInput = new LeenServletInputStream(input);
		servletReader = new BufferedReader(new InputStreamReader(servletInput),
				1);
	}

	public void parse() throws RequestException, IOException {
		parseRequestLine();
		parseHeader();
		if (method.equalsIgnoreCase("POST")) {
			if (getHeader("content-length") == null)
				throw new RequestException();
			contentLength = getIntHeader("content-length");
			servletInput.setContentLength(contentLength);
		}
		parseCookie();
		String contentTypeStr = getHeader(CommonInfo.CONTENT_TYPE);
		if (contentTypeStr != null)
			contentType.setContentType(contentTypeStr);
		// 获取host,K004.3
		host = getHeader(HOST_HEADER_NAME);
		log.debug("parsed host:" + host);
		if (host == null)
			throw new RequestException();

	}

	private void parseRequestLine() throws RequestException {
		String requestLine = null;
		try {
			requestLine = servletReader.readLine();
			if (requestLine == null || requestLine.equals(""))
				throw new RequestException();
			String[] splits = requestLine.split(" ");
			if (splits.length != 3)
				throw new RequestException();
			if (splits[0].equals("GET") || splits[0].equals("POST"))
				method = splits[0];
			else
				throw new RequestException("unsupported method");
			currentURI.setRawURI(splits[1]);
			if (splits[2].equals("HTTP/1.0") || splits[2].equals("HTTP/1.1"))
				protocol = splits[2];
			else
				throw new RequestException("unsupported protocol");
		} catch (IOException e) {
			// because of the disconnection
			throw new RequestException("client abort");
		}
		if (requestLine == null || requestLine.equals(""))
			throw new RequestException();
		try {
			requestLine = URLDecoder.decode(requestLine, "GBK");
		} catch (UnsupportedEncodingException e) {
		}
	}

	private void parseHeader() throws RequestException, IOException {
		String line = servletReader.readLine();
		while (true) {
			if (line == null || line.equals(""))
				break;
			Matcher m = headerPattern.matcher(line);
			if (!m.matches())
				throw new RequestException();
			String name = m.group(1);
			String value = m.group(2);
			line = servletReader.readLine();
			if (line != null && !line.equals("")) {
				m = nextLineValuePattern.matcher(line);
				if (m.matches()) {
					value += m.group(2);
				}
			}
			name = name.toLowerCase();
			value = value.toLowerCase();
			log.debug("header(" + name + "," + value + ")");
			List<String> list = null;
			if (headerMap.get(name) == null) {
				list = new ArrayList<String>();
				list.add(value);
				headerMap.put(name, list);
			} else {
				list = headerMap.get(name);
				list.add(value);
			}
		}
	}

	private void parseParameters() {
		if (parameterParsed)
			return;
		String queryString = currentURI.getQueryString();
		log.debug("uri query string:" + queryString);
		if (queryString != null)
			pushQueryString(queryString);
		if (method.equals("POST")) {
			if (!contentType.getContentType().equals(
					"application/x-www-form-urlencoded"))
				return;
			try {
				getReader();
				StringBuffer buffer = new StringBuffer();
				char c;
				int b;
				while ((b = servletReader.read()) != -1) {
					c = (char) b;
					buffer.append(c);
				}
				queryString = buffer.toString();
			} catch (IOException e) {
				log.debug("client abort");
			}
		}
		if (queryString == null)
			return;
		pushQueryString(queryString);
		parameterParsed = true;
	}

	public void pushQueryString(String queryString) {
		if (queryString == null)
			throw new IllegalArgumentException("query string required");
		try {
			queryString = URLDecoder
					.decode(queryString, getCharacterEncoding());
			parameters.push(queryString);
		} catch (UnsupportedEncodingException e) {
			// impossible
			throw new RuntimeException();
		}
	}

	public void popQueryString() {
		parameters.pop();
	}

	private void parseCookie() {

		String value = getHeader("cookie");
		if (value == null) {
			this.cookies = new Cookie[0];
			return;
		}

		List<Cookie> cookies = new ArrayList<Cookie>();
		String[] pairs = value.split("; ");
		for (String pair : pairs) {
			String[] splits = pair.split("=");
			if (splits.length < 2)
				continue;
			Cookie cookie = new Cookie(splits[0], splits[1]);
			if (cookie.getName().equalsIgnoreCase("sessionId")) {
				sessionId = cookie.getValue();
			}
			cookies.add(cookie);
		}
		this.cookies = cookies.toArray(new Cookie[0]);
	}

	public void setServletPath(String servletPath) {
		currentURI.setServletPath(servletPath);
	}

	public String getSubUrl() {
		return facadeURI.getSubUrl();
	}

	public String getSubUrlForMapping() {
		return currentURI.getSubUrl();
	}

	public String getSubUrlOfCurrentDispatch() {
		return currentDispatchURI.getSubUrl();
	}

	@Override
	public Object getAttribute(String name) {
		if (name == null)
			throw new IllegalArgumentException();
		if (name.equals(RequestDispatcher.FORWARD_CONTEXT_PATH))
			return currentDispatchURI.getContextPath();
		if (name.equals(RequestDispatcher.FORWARD_PATH_INFO))
			return currentDispatchURI.getPathInfo();
		if (name.equals(RequestDispatcher.FORWARD_QUERY_STRING))
			return currentDispatchURI.getQueryString();
		if (name.equals(RequestDispatcher.FORWARD_REQUEST_URI))
			return currentDispatchURI.getURI();
		if (name.equals(RequestDispatcher.FORWARD_SERVLET_PATH))
			return currentDispatchURI.getServletPath();
		if (name.equals(RequestDispatcher.INCLUDE_CONTEXT_PATH))
			return currentURI.getContextPath();
		if (name.equals(RequestDispatcher.INCLUDE_PATH_INFO))
			return currentURI.getPathInfo();
		if (name.equals(RequestDispatcher.INCLUDE_QUERY_STRING))
			return currentURI.getQueryString();
		if (name.equals(RequestDispatcher.INCLUDE_REQUEST_URI))
			return currentURI.getURI();
		if (name.equals(RequestDispatcher.INCLUDE_SERVLET_PATH))
			return currentURI.getServletPath();

		if (lastDispatchURI != null) {
			if (name.equals(AsyncContext.ASYNC_CONTEXT_PATH))
				return lastDispatchURI.getContextPath();
			if (name.equals(AsyncContext.ASYNC_PATH_INFO))
				return lastDispatchURI.getPathInfo();
			if (name.equals(AsyncContext.ASYNC_QUERY_STRING))
				return lastDispatchURI.getQueryString();
			if (name.equals(AsyncContext.ASYNC_REQUEST_URI))
				return lastDispatchURI.getURI();
			if (name.equals(AsyncContext.ASYNC_SERVLET_PATH))
				return lastDispatchURI.getServletPath();
		}

		return attributes.get(name);
	}

	private Collection<String> baseAttributeNames = new HashSet<String>(
			Arrays.asList(new String[] {
					RequestDispatcher.FORWARD_CONTEXT_PATH,
					RequestDispatcher.FORWARD_PATH_INFO,
					RequestDispatcher.FORWARD_QUERY_STRING,
					RequestDispatcher.FORWARD_REQUEST_URI,
					RequestDispatcher.FORWARD_SERVLET_PATH,
					RequestDispatcher.INCLUDE_CONTEXT_PATH,
					RequestDispatcher.INCLUDE_PATH_INFO,
					RequestDispatcher.INCLUDE_QUERY_STRING,
					RequestDispatcher.INCLUDE_REQUEST_URI,
					RequestDispatcher.INCLUDE_SERVLET_PATH }));

	@Override
	public Enumeration<String> getAttributeNames() {
		return new RequestAttributesNameEnum();
	}

	class RequestAttributesNameEnum implements Enumeration<String> {

		private Iterator<String> baseAttributeNamesIt;
		private Iterator<String> realAttributeNamesIt;
		private boolean beginrealAttributeNamesIt;

		public RequestAttributesNameEnum() {
			baseAttributeNamesIt = baseAttributeNames.iterator();
			realAttributeNamesIt = attributes.keySet().iterator();
		}

		@Override
		public boolean hasMoreElements() {
			if (beginrealAttributeNamesIt && !realAttributeNamesIt.hasNext())
				return false;
			return true;
		}

		@Override
		public String nextElement() {
			if (!beginrealAttributeNamesIt) {
				if (baseAttributeNamesIt.hasNext())
					return baseAttributeNamesIt.next();
				else
					beginrealAttributeNamesIt = true;
			}
			if (beginrealAttributeNamesIt) {
				if (realAttributeNamesIt.hasNext())
					return realAttributeNamesIt.next();
			}
			throw new NoSuchElementException();
		}

	}

	@Override
	public String getCharacterEncoding() {
		return encoding;
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		Charset.forName(env);
		this.encoding = env;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return contentType.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (useReader)
			throw new IllegalStateException();
		if (!useInputStream)
			useInputStream = true;
		return servletInput;
	}

	@Override
	public String getParameter(String name) {
		if (!parameterParsed)
			parseParameters();
		return parameters.getParameter(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return parameters.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		if (!parameterParsed)
			parseParameters();
		return parameters.getParameters(name).toArray(new String[0]);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		if (!parameterParsed)
			parseParameters();
		return parameters.getParameterMap();
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return serverInfo.getScheme();
	}

	@Override
	public String getServerName() {
		return serverInfo.getServerName();
	}

	@Override
	public int getServerPort() {
		return serverInfo.getPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (useInputStream)
			throw new IllegalStateException();
		if (servletReader != null)
			return servletReader;
		String encoding = getCharacterEncoding();
		if (encoding == null)
			encoding = System.getProperty("file.encoding");
		servletReader = new BufferedReader(new InputStreamReader(servletInput,
				encoding));
		useReader = true;
		return servletReader;
	}


	@Override
	public String getRemoteAddr() {
		return inetInfo.getRemoteAddr().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return inetInfo.getRemoteAddr().getHostName();
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		Set<Locale> locales = new HashSet<Locale>();
		locales.add(Locale.ENGLISH);
		return new CollectionEnumeration<Locale>(locales);
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		if (path == null)
			throw new IllegalArgumentException("path required");
		if (!path.startsWith("/"))
			path = currentURI.getFolder() + path;
		log.debug("dispatch to path " + path);
		return context.getRequestDispatcher(path);
	}

	@Override
	public String getRealPath(String path) {
		return context.getRealPath(path);
	}

	@Override
	public int getRemotePort() {
		return inetInfo.getRemotePort();
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		return inetInfo.getLocalAddr().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return inetInfo.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	// ***************************K001.2
	// startAsync*********************************
	private LeenAsyncContext asyncContext;

	@Override
	public AsyncContext startAsync() {
		if (asyncContext == null) {
			createAsyncContext(this, response);
		} else if (!asyncContext.isStarted())
			asyncContext.startAsync();
		asyncContext.setUsingCurrentURI(false);
		return asyncContext;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest,
			ServletResponse servletResponse) {
		if (servletRequest == null || servletResponse == null
				|| !(servletRequest instanceof HttpServletRequest)
				|| !(servletResponse instanceof HttpServletResponse))
			throw new IllegalArgumentException();
		if (asyncContext == null) {
			createAsyncContext((HttpServletRequest) servletRequest,
					(HttpServletResponse) servletResponse);
		} else if (!asyncContext.isStarted())
			asyncContext.startAsync();
		asyncContext.setRequest((HttpServletRequest) servletRequest);
		asyncContext.setResponse((HttpServletResponse) servletResponse);
		asyncContext.setUsingCurrentURI(true);
		return asyncContext;
	}

	private synchronized void createAsyncContext(HttpServletRequest request,
			HttpServletResponse response) {
		if (asyncContext != null)
			return;
		asyncContext = new LeenAsyncContext();
		asyncContext.setRequest(request);
		asyncContext.setResponse(response);
		asyncContext.setContext(context);
		asyncContext.setTimeout(CommonInfo.DEFAULT_ASYNC_TIMEOUT);
	}

	// *****************************end K001.2**************************
	@Override
	public boolean isAsyncStarted() {
		if (asyncContext == null)
			return false;
		return asyncContext.isStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LeenAsyncContext getAsyncContext() {
		return asyncContext;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return dispatcherType;
	}

	public void setDispatcherType(DispatcherType dispatcherType) {
		this.dispatcherType = dispatcherType;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	@Override
	public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		if (name.equals(HOST_HEADER_NAME) && host != null)
			return host;
		List<String> list = headerMap.get(name);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return new CollectionEnumeration<String>(headerMap.get(name));
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new CollectionEnumeration<String>(headerMap.keySet());
	}

	@Override
	public int getIntHeader(String name) {
		String headerValue = getHeader(name);
		if (headerValue == null)
			return -1;
		return Integer.parseInt(headerValue);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return facadeURI.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		return facadeURI.getContextPath();
	}

	@Override
	public String getQueryString() {
		return facadeURI.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return sessionId;
	}

	@Override
	public String getRequestURI() {
		return facadeURI.getURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer().append(facadeURI.getURI());
	}

	@Override
	public String getServletPath() {
		return facadeURI.getServletPath();
	}

	@Override
	public HttpSession getSession(boolean create) {
		HttpSession session = null;
		if (sessionId == null) {
			if (create) {
				if (response.isCommitted())
					throw new IllegalStateException(
							"cann't create session because response already committed");
				session = sessionManager.createSession();
				sessionId = session.getId();
				Cookie cookie = new Cookie("sessionId", sessionId);
				// FIXME:add path and domin to the cookie
				response.addCookie(cookie);
			}
		} else {
			session = sessionManager.findSession(sessionId);
			if (session == null) {
				if (create) {
					session = sessionManager.createSession();
					sessionId = session.getId();
					Cookie cookie = new Cookie("sessionId", sessionId);
					// FIXME:add path and domin to the cookie
					response.addCookie(cookie);
				}
			}
		}
		return session;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException,
			IllegalStateException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, IllegalStateException,
			ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public LeenResponse getResponse() {
		return response;
	}

	public void setResponse(LeenResponse response) {
		this.response = response;
	}

	public void setContext(ILeenContext leenContext) {
		this.context = leenContext;
		currentURI.setContextPath(context.getContextPath());
	}

	public HttpServletRequest getWrapper() {
		if (wrapperThreadLocal.get() != null)
			return wrapperThreadLocal.get();
		else
			return this;
	}

	public void setWrapper(HttpServletRequestWrapper wrapper) {
		if (wrapper == null)
			throw new IllegalArgumentException("warpper required");
		this.wrapperThreadLocal.set(wrapper);
	}

	public String toAbsolute(String relative) {
		return "http://" + host + facadeURI.toAbsolute(relative);
	}

	private InetInfo inetInfo;

	public void setInetInfo(InetInfo inetInfo) {
		if(inetInfo==null)
			throw new IllegalArgumentException();
		this.inetInfo=inetInfo;
	}
}
