package leen.sc.filter;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

public abstract class MappingData {
	private Filter filter;
	private EnumSet<DispatcherType> dTypes;
	private boolean isMatchAfter;
	private boolean isProgramedIn=false;

	public MappingData(Filter filter,EnumSet<DispatcherType> dTypes,boolean isProgramedIn){
		this.filter=filter;
		this.dTypes=dTypes;
		this.isProgramedIn=isProgramedIn;
	}
	
	public MappingData(Filter filter,EnumSet<DispatcherType> dTypes,boolean isProgramedIn,boolean isMatchAfter){
		this.filter=filter;
		this.dTypes=dTypes;
		this.isProgramedIn=isProgramedIn;
		this.isMatchAfter=isMatchAfter;
	}
	
	public Filter getFilter() {
		return filter;
	}

	public EnumSet<DispatcherType> getdTypes() {
		return dTypes;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setdTypes(EnumSet<DispatcherType> dTypes) {
		this.dTypes = dTypes;
	}

	public boolean isMatchAfter() {
		return isMatchAfter;
	}

	public boolean isProgramedIn() {
		return isProgramedIn;
	}
	
}
