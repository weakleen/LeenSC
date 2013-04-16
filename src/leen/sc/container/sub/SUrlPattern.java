package leen.sc.container.sub;

import leen.sc.container.MalFormedUrlPatternException;
import leen.sc.servlet.ServletWrapper;

public class SUrlPattern extends UrlPattern {

	private ServletWrapper wrapper;
	
	public SUrlPattern(String urlPattern,ServletWrapper wrapper) throws MalFormedUrlPatternException {
		super(urlPattern);
		this.wrapper=wrapper;
	}
	
	public ServletWrapper getWrapper(){
		return wrapper;
	}
	
	
	
}
