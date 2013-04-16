package leen.sc.filter;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

public class UrlPatternFilterMappingData extends MappingData{
	private String urlPattern;

	public UrlPatternFilterMappingData(Filter filter,String urlPattern,
			EnumSet<DispatcherType> dispatcherTypes,boolean isProgrammedIn,boolean isMatchedAfter) {
		super(filter,dispatcherTypes,isProgrammedIn,isMatchedAfter);
		this.urlPattern = urlPattern;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public boolean match(String url,DispatcherType dispatcherType) {
		if(url==null||dispatcherType==null)
			throw new IllegalArgumentException();
		if(!getdTypes().contains(dispatcherType))
			return false;
		if (urlPattern.indexOf('*') == -1)
			if (url.equals(urlPattern))
				return true;
			else
				return false; 
		else if (urlPattern.startsWith("*.")) {
			if (url.endsWith(urlPattern.substring(2)))
				return true;
			else
				return false;
		} else if (urlPattern.endsWith("*")) {
			if (url.startsWith(urlPattern.substring(0, urlPattern.length() - 1)))
				return true;
			else
				return false;
		}
		else
			throw new IllegalStateException("illegal url-pattern");
	}

}
