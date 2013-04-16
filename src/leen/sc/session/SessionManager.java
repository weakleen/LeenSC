package leen.sc.session;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionManager {
	private Map<String, LeenHttpSession> sessions = new HashMap<String, LeenHttpSession>();
	private List<HttpSessionListener> listeners = new ArrayList<HttpSessionListener>();
	private int sessionIdLength = 16;
	private int maxInactiveInterval = 1800;
	private BackgroundThread backgroundThread;
	private int checkInterval = 5000;
	private boolean isStarted;

	public SessionManager() {
	}

	public HttpSession findSession(String id) {
		if (id == null)
			throw new IllegalArgumentException("sessionId can not be null");
		LeenHttpSession session = sessions.get(id.toUpperCase());
		if (session == null || !session.isValid())
			return null;
		session.access();
		return session;
	}

	public HttpSession createSession() {
		LeenHttpSession session = new LeenHttpSession(generateSessionId());
		session.setMaxInactiveInterval(maxInactiveInterval);
		sessions.put(session.getId(), session);
		HttpSessionEvent event = new HttpSessionEvent(session);
		for (HttpSessionListener listener : listeners)
			listener.sessionCreated(event);
		return session;
	}

	public void backgroundProcess() {
		for (LeenHttpSession session : sessions.values()) {
			long idle = System.currentTimeMillis()
					- session.getLastAccessedTime() * 1000L;
			if (idle > session.getMaxInactiveInterval() * 1000L) {
				session.invalidate();
				sessions.remove(session.getId());
				HttpSessionEvent event = new HttpSessionEvent(session);
				for (HttpSessionListener listener : listeners)
					listener.sessionDestroyed(event);
			}
		}
	}

	private String generateSessionId() {
		SecureRandom random = new SecureRandom();
		StringBuffer buffer = new StringBuffer();
		int length = 0;
		byte[] bytes = new byte[16];
		while (length < sessionIdLength) {
			random.nextBytes(bytes);
			for (int i = 0; i < bytes.length && length < sessionIdLength; i++) {
				byte b = bytes[i];
				byte b1 = (byte) ((b & 0xf0) >> 4);
				byte b2 = (byte) (b & 0x0f);
				if (b1 < 10) {
					buffer.append((char) ('0' + b1));
				} else {
					buffer.append((char) ('A' + b1 - 10));
				}
				if (b2 < 10) {
					buffer.append((char) ('0' + b2));
				} else {
					buffer.append((char) ('A' + b2 - 10));
				}
				length++;
			}
		}
		return buffer.toString();
	}

	public void addListener(HttpSessionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(HttpSessionListener listener) {
		listeners.remove(listener);
	}

	public void start() {
		if (isStarted)
			throw new IllegalStateException("already started");
		backgroundThread = new BackgroundThread(this);
		backgroundThread.start();
		isStarted = true;
	}

	public void stop() {
		if (isStarted) {
			backgroundThread.shutdown();
			isStarted = false;
		}
	}

	// property accessors

	public int getSessionIdLength() {
		return sessionIdLength;
	}

	public void setSessionIdLength(int sessionIdLength) {
		this.sessionIdLength = sessionIdLength;
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public boolean isStarted() {
		return isStarted;
	}

}

class BackgroundThread extends Thread {
	private SessionManager manager;
	private boolean stopped = false;

	public BackgroundThread(SessionManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				manager.backgroundProcess();
				Thread.sleep(manager.getCheckInterval());
			} catch (InterruptedException e) {
				if (stopped)
					break;
			}
		}
	}

	public void shutdown() {
		stopped = true;
		this.interrupt();
	}

}