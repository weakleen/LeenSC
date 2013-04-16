package leen.sc.filter;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

public class ServletNameFilterMappingData extends MappingData{
	private String servletName;

	public ServletNameFilterMappingData(Filter filter,String servletName,
			EnumSet<DispatcherType> dispatcherTypes,boolean isProgrammedIn,boolean isMatchedAfter) {
		super(filter,dispatcherTypes,isProgrammedIn,isMatchedAfter);
		this.servletName = servletName;
	}

	public String getServletName() {
		return servletName;
	}


	public boolean match(String servletName, DispatcherType dispatcherType) {
		if(servletName==null||dispatcherType==null)
			throw new IllegalArgumentException();
		if (getdTypes().contains(dispatcherType)
				&& servletName.equals(servletName))
			return true;
		return false;
	}
}
