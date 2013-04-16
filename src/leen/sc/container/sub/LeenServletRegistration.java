package leen.sc.container.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import leen.sc.container.MalFormedUrlPatternException;
import leen.sc.servlet.ServletWrapper;
import leen.sc.servlet.ServletWrapperManager;

public class LeenServletRegistration implements ServletRegistration.Dynamic {
	private List<String> mappings = new ArrayList<String>();
	private String runAsRole;

	private ServletWrapper wrapper;
	private ServletWrapperManager wrapperMgr;

	public LeenServletRegistration(ServletWrapper wrapper,
			ServletWrapperManager wrapperMgr) {
		this.wrapper = wrapper;
		this.wrapperMgr = wrapperMgr;
	}

	@Override
	public String getName() {
		return wrapper.getServletName();
	}

	@Override
	public String getClassName() {
		return wrapper.getClassName();
	}

	public void setName(String name) {
		wrapper.setServletName(name);
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		if (wrapper.getInitParameter(name) != null)
			return false;
		wrapper.setInitParameter(name, value);
		return true;
	}

	@Override
	public String getInitParameter(String name) {
		return wrapper.getInitParameter(name);
	}

	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		Set<String> conflicts = new HashSet<String>();
		for (String name : initParameters.keySet()) {
			if (!setInitParameter(name, initParameters.get(name)))
				conflicts.add(name);
		}
		return conflicts;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return wrapper.getInitParameters();
	}

	@Override
	public Set<String> addMapping(String... urlPatternStrs) {
		Set<String> conflicts = new HashSet<String>();
		for (String urlPatternStr : urlPatternStrs) {
			SUrlPattern sPattern = null;
			try {
				sPattern = new SUrlPattern(urlPatternStr, wrapper);
			} catch (MalFormedUrlPatternException e) {
				throw new IllegalArgumentException();
			}
			if (wrapperMgr.addSUrlPattern(sPattern))
				mappings.add(urlPatternStr);
			else
				conflicts.add(urlPatternStr);
		}
		return conflicts;
	}

	@Override
	public Collection<String> getMappings() {
		return mappings;
	}

	@Override
	public String getRunAsRole() {
		return runAsRole;
	}

	@Override
	public void setAsyncSupported(boolean isAsyncSupported) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoadOnStartup(int loadOnStartup) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMultipartConfig(MultipartConfigElement multipartConfig) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRunAsRole(String roleName) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> setServletSecurity(ServletSecurityElement constraint) {
		// TODO Auto-generated method stub
		return null;
	}

}
