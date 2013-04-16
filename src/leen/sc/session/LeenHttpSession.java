package leen.sc.session;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionListener;

public class LeenHttpSession implements HttpSession {
	private String id;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private long creationTime;
	private int maxInactiveInterval;
	private boolean isNew;
	private long lastAccessedTime;
	private boolean isValid;

	public LeenHttpSession(String id) {
		isValid = true;
		isNew = true;
		this.id = id;
		access();
	}

	public boolean isValid() {
		return isValid;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String getId() {
		return id;
	}

	public void access() {
		if (!isValid)
			throw new IllegalStateException("session expired");
		isNew = false;
		lastAccessedTime = System.currentTimeMillis() / 1000;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		if (!isValid)
			throw new IllegalStateException("session expired");
		return attributes.get(name);
	}

	@Override
	@Deprecated
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		if (!isValid)
			throw new IllegalStateException("session expired");
		return new Enumeration<String>() {
			Iterator<String> it = attributes.keySet().iterator();

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

	@Override
	public String[] getValueNames() {
		if (!isValid)
			throw new IllegalStateException("session expired");
		return attributes.keySet().toArray(new String[0]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (!isValid)
			throw new IllegalStateException("session expired");
		if (name == null)
			throw new IllegalArgumentException("name can not be null");
		Object oldValue = attributes.get(name);
		attributes.put(name, value);
		if (value instanceof HttpSessionBindingListener) {
			HttpSessionBindingListener listener = (HttpSessionBindingListener) value;
			listener.valueBound(new HttpSessionBindingEvent(this, name, value));
		}
		if (oldValue == null) {
			for (Object attribute : attributes.values()) {
				if (attribute instanceof HttpSessionAttributeListener) {
					HttpSessionAttributeListener listener = (HttpSessionAttributeListener) attribute;
					HttpSessionBindingEvent event = new HttpSessionBindingEvent(
							this, name, value);
					listener.attributeAdded(event);
				}
			}
		} else {
			if (oldValue instanceof HttpSessionBindingListener) {
				HttpSessionBindingListener listener = (HttpSessionBindingListener) oldValue;
				listener.valueUnbound(new HttpSessionBindingEvent(this, name,
						oldValue));
			}
			for (Object attribute : attributes.values()) {
				if (attribute instanceof HttpSessionAttributeListener) {
					HttpSessionAttributeListener listener = (HttpSessionAttributeListener) attribute;
					HttpSessionBindingEvent event = new HttpSessionBindingEvent(
							this, name, oldValue);
					listener.attributeReplaced(event);
				}
			}
		}
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		if (!isValid)
			throw new IllegalStateException("session expired");
		if (name == null)
			throw new IllegalArgumentException("name can not be null");
		Object value = attributes.remove(name);
		if (value == null)
			return;
		HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name,
				value);
		if (value instanceof HttpSessionBindingListener) {
			((HttpSessionBindingListener) value).valueUnbound(event);
		}
		for (Object attribute : attributes.values()) {
			if (attribute instanceof HttpSessionAttributeListener)
				((HttpSessionAttributeListener) attribute)
						.attributeRemoved(event);
		}
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}

	@Override
	public void invalidate() {
		if (!isValid)
			throw new IllegalStateException("session expired");
		isValid = false;
		attributes.clear();
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

}
