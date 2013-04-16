package leen.sc.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class LeenFilterChain implements FilterChain {
	private Logger log=Logger.getLogger(LeenFilterChain.class);
	
	private HttpServlet servlet;
	private List<Filter> filterList;
	private int position=0;
	
	public LeenFilterChain(List<Filter> filterList){
		this.filterList=filterList;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		if(position==filterList.size()){
			servlet.service(request, response);
		}
		else if(position<filterList.size()){
			Filter filter=filterList.get(position++);
			filter.doFilter(request, response, this);
		}
		else
			throw new IllegalStateException();
	}

	public void setServlet(HttpServlet servlet) {
		this.servlet = servlet;
	}

}
