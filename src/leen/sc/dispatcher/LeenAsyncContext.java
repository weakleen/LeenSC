package leen.sc.dispatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leen.sc.container.ILeenContext;
import leen.sc.container.LeenContext;
import leen.sc.dispatcher.AsyncStateMachine.AsyncState;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.LeenResponse;
import leen.sc.util.Retriever;

import org.apache.log4j.Logger;

public class LeenAsyncContext implements AsyncContext, TimeCycle {

	private static Logger log = Logger.getLogger(LeenAsyncContext.class);

	private LeenRequest request;
	private LeenResponse response;
	private ILeenContext context;
	private AsyncStateMachine stateMachine = new AsyncStateMachine();
	private ILeenContext chosenContext;
	private Runnable lastTask;
	private Set<AsyncListener> listeners = new HashSet<AsyncListener>();
	private String path;
	private Map<AsyncListener, AsyncEvent> eventMap = new HashMap<AsyncListener, AsyncEvent>();

	private long timeOut;
	private long remainTime;

	public LeenAsyncContext(HttpServletRequest request,
			HttpServletResponse response, ILeenContext context, long timeOut,
			boolean usingCurrentURI) {
		if (request == null || response == null || context == null)
			throw new IllegalArgumentException("cann't input null argument");
		this.request = Retriever.retrieveRequest(request);
		this.response = Retriever.retrieveResponse(response);
		this.context = context;
		this.timeOut = timeOut;
		this.usingCurrentURI = usingCurrentURI;
	}

	public LeenAsyncContext() {
	}

	public void postService() {
		stateMachine.postService();
		log.debug("stateMachine.isDispatching():"
				+ stateMachine.isDispatching() + ",state:"
				+ stateMachine.getState());
		if (isStarted()) {
			startTimeout();
		} else if (stateMachine.isDispatching()) {
			log.debug("before real dispatch");
			realDispatch();
		} else {
			if (!isCompleted())
				complete();
		}

	}

	// K001.2.5
	public boolean isStarted() {
		return stateMachine.isStarted();
	}

	// K001.2.4
	public synchronized void startAsync() {
		stateMachine.startAsync();
	}

	public boolean isCompleted() {
		return stateMachine.isCompleted();
	}

	// ����listener#onTimeout
	// ���û��listener����complete,��dispatch��error page
	// ����Ҳ���error page,����complete
	// ���timeout>0������timeout����
	public boolean timeEvent() {
		remainTime -= TIME_INTERVAL;
		if (remainTime > 0)
			return false;

		for (AsyncListener listener : listeners) {
			try {
				listener.onTimeout(eventMap.get(listener));
			} catch (IOException e) {
			}
		}

		if (!stateMachine.isCompleted()) {
			log.debug("start forward");
			RequestDispatcher rd = request
					.getRequestDispatcher("/aysncTimeout");

			// �Ҳ���error page
			if (rd == null) {
				complete();
				return true;
			}

			try {
				rd.forward(request, response);

			} catch (Throwable e) {
				// �κ��쳣�����ٴ��������ͻ��˶Ͽ�����
				complete();
				response.finish();
			}
		}

		if (!isCompleted())
			complete();
		return true;
	}

	private void startTimeout() {
		if (timeOut <= 0)
			return;
		remainTime = timeOut;
		if (!TimeCycleProcessor.getInstance().existTimeCycle(this)) {
			TimeCycleProcessor.getInstance().addTimeCycle(this);
			log.debug("timeout started,remainTime");
		}
	}

	private void stopTimeout() {
		if (TimeCycleProcessor.getInstance().existTimeCycle(this))
			TimeCycleProcessor.getInstance().removeTimeCycle(this);
	}

	public void setRequest(HttpServletRequest request) {
		if (request == null)
			throw new IllegalArgumentException("request required");
		this.request = Retriever.retrieveRequest(request);
	}

	public void setResponse(HttpServletResponse response) {
		if (response == null)
			throw new IllegalArgumentException("response required");
		this.response = Retriever.retrieveResponse(response);
	}

	@Override
	public ServletRequest getRequest() {
		return request.getWrapper();
	}

	@Override
	public ServletResponse getResponse() {
		return response.getWrapper();
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean usingCurrentURI;

	public void setUsingCurrentURI(boolean usingCurrentURI) {
		this.usingCurrentURI = usingCurrentURI;
	}

	public boolean isUsingCurrentURI() {
		return usingCurrentURI;
	}

	// K001.1
	@Override
	public void dispatch() {
		if (usingCurrentURI)
			dispatch(request.getSubUrl());
		else
			dispatch(request.getSubUrlOfCurrentDispatch());
	}

	@Override
	public void dispatch(String path) {
		dispatch(context, path);
	}

	// 1����������
	// 2������path
	// 3��query string
	// 4������ԭʼpath
	// 5���ı�״̬��״̬
	// 6��ֹͣtimeout��ʱ
	@Override
	public void dispatch(ServletContext context, String path) {
		if (!(context instanceof LeenContext))
			throw new IllegalArgumentException("bad context");
		stateMachine.dispatch();
		this.chosenContext = (ILeenContext) context;
		this.path = path;
		if (stateMachine.isDispatching()) {
			stopTimeout();
			realDispatch();
		}
	}

	private void realDispatch() {
		chosenContext.getExecutor().execute(
				new DispatchTask((LeenContext) chosenContext, path));
	}

	class DispatchTask implements Runnable {

		private LeenContext taskContext;
		private RequestURI uri;

		public DispatchTask(LeenContext context, String path) {
			this.taskContext = context;
			uri = new RequestURI();
			uri.setRawURI(context.getContextPath() + path);
			uri.setContextPath(context.getContextPath());
		}

		@Override
		public void run() {
			synchronized (LeenAsyncContext.this) {
				lastTask = this;
			}

			// ����DispatcherType
			request.setDispatcherType(DispatcherType.ASYNC);
			
			request.setAsyncAttributes(uri);
			if (uri.getQueryString() != null)
				request.pushQueryString(uri.getQueryString());

			// ��������
			try {
				stateMachine.dispatched();
				taskContext.leen(request.getWrapper(), response.getWrapper());
				log.debug("request.state:" + stateMachine.getState()
						+ ",isStarted:" + request.isAsyncStarted());
			} catch (Exception e1) {
				e1.printStackTrace();
				/**
				 * error handling 1����������listener��onError����
				 * 2�����û��listener����dispatch��complete��������ôdispatch������ҳ��
				 */
				for (AsyncListener listener : listeners) {
					try {
						listener.onError(new AsyncEvent(LeenAsyncContext.this,
								e1));
					} catch (IOException e) {
					}
				}
				request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e1);
				if (lastTask == this && !stateMachine.isCompleted())
					;
				// TODO dispatch to error page
				return;
			} finally {
			}

			/*
			 * // δ�����쳣��ر�response boolean close = false; synchronized
			 * (LeenAsyncContext.this) { if (this == lastTask) close = true; }
			 * if (close) { Retriever.retrieveResponse(response).close(); }
			 */

		}

	}

	// 1���ر�response
	// 2������״̬��״̬Ϊcomplete
	// 3���ṩevent
	// 4��listeners.onComplete
	// ֹͣtimeout��ʱ
	// X��onComplete�쳣����
	@Override
	public void complete() {

		stateMachine.complete();
		for (AsyncListener listener : listeners) {
			AsyncEvent event = eventMap.get(listener);
			try {
				listener.onComplete(event);
			} catch (IOException e) {
				// TODO
			}
		}
		Retriever.retrieveResponse(response).finish();
		stopTimeout();
	}

	@Override
	public void start(Runnable run) {
		context.getExecutor().execute(run);
	}

	// ������״̬�²�����ִ�и÷�����Dispatching��AsyncWait��Completed
	@Override
	public void addListener(AsyncListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("null input not permitted");
		if (!stateMachine.allowAddListener())
			throw new IllegalArgumentException();
		listeners.add(listener);
		AsyncEvent event = new AsyncEvent(this);
		eventMap.put(listener, event);
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest request,
			ServletResponse response) {
		if (listener == null || request == null || response == null)
			throw new IllegalArgumentException("null input not permitted");
		if (!stateMachine.allowAddListener())
			throw new IllegalArgumentException();
		listeners.add(listener);
		AsyncEvent event = new AsyncEvent(this, request, response);
		eventMap.put(listener, event);
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeout() {
		return this.timeOut;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeOut = timeout;
	}

	public void setContext(ILeenContext context) {
		if (context == null)
			throw new IllegalArgumentException();
		this.context = context;
	}

	public ILeenContext getContext() {
		return context;
	}

	public AsyncState getState() {
		return stateMachine.getState();
	}

}
