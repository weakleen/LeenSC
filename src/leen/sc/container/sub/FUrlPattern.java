package leen.sc.container.sub;

import leen.sc.container.MalFormedUrlPatternException;
import leen.sc.filter.LeenFilterWrapper;

public class FUrlPattern extends UrlPattern {

	private LeenFilterWrapper wrapper;
	
	public FUrlPattern(String urlPattern) throws MalFormedUrlPatternException {
		super(urlPattern);
	}
	
	public void setWrapeer(LeenFilterWrapper wrapper){
		this.wrapper=wrapper;
	}
	
	public LeenFilterWrapper getWrapper(){
		return wrapper;
	}
	
}
