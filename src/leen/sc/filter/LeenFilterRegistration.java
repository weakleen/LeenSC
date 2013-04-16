package leen.sc.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

public class LeenFilterRegistration implements FilterRegistration {

	private String name;
	private String className;
	private Map<String, String> initParameters;
	private List<ServletNameFilterMappingData> smappings;
	private List<UrlPatternFilterMappingData> umappings;
	private Filter filter;

	public LeenFilterRegistration(Filter filter) {
		this.filter = filter;
	}

	public LeenFilterRegistration(String name, String className,
			Map<String, String> initParameters,
			Collection<String> servletNameMappings,
			Collection<String> urlPatterns) {
		if (name == null || name.equals("") || className == null
				|| className.equals("") || initParameters == null
				|| servletNameMappings == null || urlPatterns == null)
			throw new IllegalArgumentException();
		this.name = name;
		this.className = className;
		this.initParameters = initParameters;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return false;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		Set<String> conflict=new HashSet<String>();
		for(String name:initParameters.keySet()){
			if(!setInitParameter(name, initParameters.get(name)))
				conflict.add(name);
		}
		return conflict;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	@Override
	public void addMappingForServletNames(
			EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
			String... servletNames) {
		for(String servletName:servletNames)
			umappings.add(new UrlPatternFilterMappingData(filter, servletName, dispatcherTypes,true,isMatchAfter));
	}

	@Override
	public Collection<String> getServletNameMappings() {
		List<String> servletNames = new ArrayList<String>();
		for (ServletNameFilterMappingData smapping : smappings)
			servletNames.add(smapping.getServletName());
		return servletNames;
	}

	@Override
	public void addMappingForUrlPatterns(
			EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
			String... urlPatterns) {
		for(String urlPattern:urlPatterns)
			umappings.add(new UrlPatternFilterMappingData(filter, urlPattern, dispatcherTypes,true,isMatchAfter));
		
	}

	@Override
	public Collection<String> getUrlPatternMappings() {
		List<String> urlPatterns = new ArrayList<String>();
		for (UrlPatternFilterMappingData umapping : umappings)
			urlPatterns .add(umapping.getUrlPattern());
		return urlPatterns ;
	}
}
