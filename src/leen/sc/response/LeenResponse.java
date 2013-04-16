package leen.sc.response;

import static leen.sc.HeaderNames.CONTENT_TYPE;
import static leen.sc.HeaderNames.SERVER;
import static leen.sc.HeaderNames.X_POWRED_BY;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;

import javax.servlet.DispatcherType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import leen.sc.request.ContentType;
import leen.sc.request.LeenRequest;
import leen.sc.startup.util.CommonInfo;

import org.apache.log4j.Logger;

public class LeenResponse implements HttpServletResponse {
	private static final Logger log = Logger.getLogger(LeenResponse.class);

	private static final String CONTENT_TYPE_HEADER_NAME = "content-type";

	private boolean useOutputStream = false;
	private boolean useWriter = false;
	private LeenServletOutputStream servletOut = null;
	private PrintWriter writer = null;
	// private OutputStream originOut;
	private LeenRequest request;
	private boolean isIncluded;
	private Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

	// Ref K004.7
	private ContentType contentType;
	private int status = 200;
	private String msg = "OK";

	private String encoding = System.getProperty("file.encoding");

	private ResponseStateMachine stateMachine = new ResponseStateMachine();

	private ThreadLocal<HttpServletResponseWrapper> wrapperThreadLocal = new ThreadLocal<HttpServletResponseWrapper>();

	/*
	 * public LeenResponse(OutputStream output) { this.originOut = output; }
	 */
	public LeenResponse(LeenServletOutputStream servletOut) {
		servletOut.setResponse(this);
		this.servletOut = servletOut;
	}

	public void prepareResponse() throws IOException {
		if (isCommitted())
			throw new IllegalStateException("already committed");
		String responseLine = request.getProtocol() + " " + status + " " + msg
				+ "\r\n";
		genDefaultHeaders();
		StringBuffer headerBuffer = new StringBuffer();
		for (String name : headerMap.keySet()) {
			List<String> values = headerMap.get(name);
			for (String value : values) {
				headerBuffer.append(name + ": " + value + "\r\n");
			}
		}
		String blankLine = "\r\n";
		String msg = responseLine + headerBuffer.toString() + blankLine;
		servletOut.prepare(msg.getBytes());
	}

	private void genDefaultHeaders() {
		setHeaderInternal(SERVER, CommonInfo.SERVER);
		if (contentType != null)
			setHeaderInternal(CONTENT_TYPE, contentType.getContentType());
		setHeaderInternal(X_POWRED_BY, "LEEN-SC");
	}

	public void setRequest(LeenRequest request) {
		this.request = request;
	}

	public void close() {
		if (stateMachine.isClosed())
			return;
		if (!stateMachine.isCommited())
			try {
				flushBuffer();
			} catch (IOException e) {
			}
		stateMachine.close();
	}

	public boolean isClosed() {
		return stateMachine.isClosed();
	}

	public void finish() {
		try {
			flushBuffer();
			if (!stateMachine.isClosed())
				close();
			stateMachine.finish();
			servletOut.close();
		} catch (IOException e) {
		}
		this.contentType = null;
		headerMap.clear();
		this.headerMap = null;
		this.msg = null;
		this.servletOut = null;
		this.wrapperThreadLocal = null;
		this.writer = null;
	}

	public boolean isFinished() {
		return stateMachine.isFinished();
	}

	public ResponseStateMachine getStateMachine() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return stateMachine;
	}

	private void setHeaderInternal(String name, String value) {
		List<String> list = null;
		if (headerMap.get(name) == null) {
			list = new ArrayList<String>();
			list.add(value);
			headerMap.put(name, list);
		} else {
			list = headerMap.get(name);
			list.clear();
			list.add(value);
		}
	}

	// codes below are implementation of the Servlet special

	@Override
	public String getCharacterEncoding() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return encoding;
	}

	@Override
	public String getContentType() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (contentType == null)
			return null;
		return contentType.getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (useWriter)
			throw new IllegalStateException("using writer");
		useOutputStream = true;
		return servletOut;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (useOutputStream)
			throw new IllegalStateException("using outputStream");
		useWriter = true;
		if (writer == null) {
			writer = new PrintWriter(new OutputStreamWriter(servletOut,
					encoding));
		}
		return writer;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		if (isCommitted() || useWriter)
			return;
		this.encoding = charset;
		if (contentType != null)
			contentType.setCharaterEncoding(encoding);
	}

	@Override
	public void setContentLength(int len) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (isIncluded)
			return;
		setIntHeader("Content-Length", len);
	}

	// Ref K004.7
	@Override
	public void setContentType(String type) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (request.getDispatcherType() == DispatcherType.INCLUDE
				|| isCommitted())
			return;
		/*
		 * try { type = URLEncoder.encode(type, getCharacterEncoding()); } catch
		 * (UnsupportedEncodingException e) { }
		 */
		if (contentType == null)
			contentType = new ContentType();
		contentType.setContentType(type);
		if (contentType.getCharacterEncoding() != null)
			setCharacterEncoding(contentType.getCharacterEncoding());
		else {
			log.debug("setting encoding");
			contentType.setCharaterEncoding(encoding);
		}
	}

	@Override
	public void setBufferSize(int size) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		servletOut.setBuffersize(size);
	}

	@Override
	public int getBufferSize() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return servletOut.getBufferSize();
	}

	@Override
	public synchronized void flushBuffer() throws IOException {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (!isCommitted()) {
			prepareResponse();
			stateMachine.commit();
		}
		if (writer != null)
			writer.flush();
		if (servletOut != null)
			servletOut.flush();
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public boolean isCommitted() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return stateMachine.isCommited();
	}

	@Override
	public void reset() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		servletOut.reset();
	}

	@Override
	public void setLocale(Locale loc) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		StringBuffer msg = new StringBuffer();
		if (cookie.getName() == null)
			return;
		msg.append(cookie.getName() + "=" + cookie.getValue());
		if (cookie.getMaxAge() != -1) {
			Date expireDate = new Date(System.currentTimeMillis()
					+ cookie.getMaxAge() * 1000);
			SimpleDateFormat format = new SimpleDateFormat(
					"EEEEEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.ENGLISH);
			Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
			format.setCalendar(cal);
			msg.append("; expires=" + format.format(expireDate));
		}
		if (cookie.getPath() != null) {
			msg.append("; path=" + cookie.getPath());
		}
		if (cookie.getDomain() != null) {
			msg.append("; domin=" + cookie.getDomain());
		}
		if (cookie.getSecure()) {
			msg.append("; secure");
		}
		addHeader("set-cookie", msg.toString());
	}

	@Override
	public boolean containsHeader(String name) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return headerMap.containsKey(name);
	}

	@Override
	public String encodeURL(String url) {
		try {
			return URLEncoder.encode(url,encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (isFinished())
			throw new IllegalStateException("response expired");
		resetBuffer();
	}

	@Override
	public void sendError(int sc) throws IOException {

	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if (isCommitted())
			throw new IllegalStateException();
		if (!location.startsWith("http://"))
			location = request.toAbsolute(location);
		resetBuffer();
		setStatus(SC_FOUND);
		setHeader("Location", location);
		close();
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDateHeader(String name, long date) {

	}

	@Override
	public void setHeader(String name, String value) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (useWriter || useOutputStream)
			throw new IllegalStateException(
					"can not set content type after getting writer or outputstream");
		if (request.getDispatcherType() == DispatcherType.INCLUDE)
			return;

		if (name.equalsIgnoreCase(CONTENT_TYPE_HEADER_NAME)) {
			setContentType(value);
			return;
		}

		/*
		 * try { value = URLEncoder.encode(value, getCharacterEncoding()); }
		 * catch (UnsupportedEncodingException e) { // ignore }
		 */

		List<String> list = null;
		if (headerMap.get(name) == null) {
			list = new ArrayList<String>();
			list.add(value);
			headerMap.put(name, list);
		} else {
			list = headerMap.get(name);
			list.clear();
			list.add(value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (isCommitted())
			throw new IllegalStateException(
					"cann't set content type after committing response");
		if (request.getDispatcherType() == DispatcherType.INCLUDE)
			return;
		/*
		 * try { value = URLEncoder.encode(value, getCharacterEncoding()); }
		 * catch (UnsupportedEncodingException e) { // ignore }
		 */
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

	@Override
	public void setIntHeader(String name, int value) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		setHeader(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		addHeader(name, String.valueOf(value));
	}

	@Override
	public void setStatus(int sc) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (isIncluded)
			return;
		String msg = null;
		switch (sc) {
		case 302:
			msg = "Found";
			break;
		case 400:
			msg = "Bad Request";
			break;
		case 404:
			msg = "Not Found";
			break;
		case 500:
			msg = "Internal Server Error";
			break;
		default:
			break;
		}
		setStatus(sc, msg);
	}

	@Override
	public void setStatus(int sc, String sm) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		this.status = sc;
		this.msg = sm;
	}

	@Override
	public int getStatus() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return status;
	}

	@Override
	public String getHeader(String name) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (name == null)
			throw new IllegalArgumentException();
		if (name.equalsIgnoreCase(CONTENT_TYPE_HEADER_NAME))
			return contentType.getContentType();
		if (headerMap.get(name) == null)
			return null;
		return headerMap.get(name).get(0);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return headerMap.get(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		return headerMap.keySet();
	}

	/*
	 * public boolean isIncluded() { return isIncluded; }
	 * 
	 * public void setIncluded(boolean isIncluded) { this.isIncluded =
	 * isIncluded; }
	 */
	public void setWrapper(HttpServletResponseWrapper wrapper) {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (wrapper == null)
			throw new IllegalArgumentException("wrapper required");
		this.wrapperThreadLocal.set(wrapper);
	}

	public HttpServletResponse getWrapper() {
		if (isFinished())
			throw new IllegalStateException("response expired");
		if (wrapperThreadLocal.get() != null)
			return wrapperThreadLocal.get();
		else
			return this;
	}

}
