package leen.sc.request;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leen.sc.util.CollectionEnumeration;

public class Parameters {
	private LinkedList<Map<String, List<String>>> parameterStack = new LinkedList<Map<String, List<String>>>();

	public void push(String queryString) {
		if (queryString == null)
			throw new IllegalArgumentException("query string required");
		Map<String, List<String>> parametersMap = new HashMap<String, List<String>>();
		String[] paramStrSplits = queryString.split("&");
		for (String paramStr : paramStrSplits) {
			String[] para_value = paramStr.split("=");
			if (para_value.length != 2)
				continue;
			else {
				if (parametersMap.get(para_value[0]) == null) {
					List<String> list = new ArrayList<String>();
					list.add(para_value[1]);
					parametersMap.put(para_value[0], list);
				} else {
					parametersMap.get(para_value[0]).add(para_value[1]);
				}
			}
		}
		parameterStack.push(parametersMap);
	}

	public void pop() {
		parameterStack.pop();
	}

	public String getParameter(String name) {
		if (name == null)
			throw new IllegalArgumentException("name required");
		for (int i = 0; i < parameterStack.size(); i++) {
			List<String> list = parameterStack.get(i).get(name);
			if (list == null || list.size() == 0)
				continue;
			else
				return list.get(0);
		}
		return null;
	}

	public List<String> getParameters(String name) {
		if (name == null)
			throw new IllegalArgumentException("name required");
		List<String> list0 = new ArrayList<String>();
		for (int i = 0; i < parameterStack.size(); i++)  {
			List<String> list = parameterStack.get(i).get(name);
			if (list0 == null || list.size() == 0)
				continue;
			else
				list0.addAll(list);
		}
		return list0;
	}

	public Enumeration<String> getParameterNames() {
		Set<String> names = new HashSet<String>();
		for (int i = parameterStack.size() - 1; i >= 0; i--) {
			Set<String> set = parameterStack.get(i).keySet();
			names.addAll(set);
		}
		return new CollectionEnumeration<String>(names);
	}

	public Map<String, String[]> getParameterMap() {
		Enumeration<String> names = getParameterNames();
		Map<String, String[]> map = new HashMap<String, String[]>();
		while (names.hasMoreElements()) {
			map.put(names.nextElement(), getParameters(names.nextElement())
					.toArray(new String[0]));
		}
		return map;
	}

	public void clear() {
		parameterStack.clear();
	}

	public void destroy() {
		parameterStack.clear();
		parameterStack = null;
	}
}
