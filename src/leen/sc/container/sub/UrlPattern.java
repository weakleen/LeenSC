package leen.sc.container.sub;

import org.apache.log4j.Logger;

import leen.sc.container.MalFormedUrlPatternException;

public class UrlPattern implements Comparable<UrlPattern>{
	private static Logger log=Logger.getLogger(UrlPattern.class);
	private String urlPattern;
	private UrlPatternType type;

	public UrlPattern(String urlPattern) throws MalFormedUrlPatternException {
		this.urlPattern = urlPattern;
		type=UrlPatternType.match(urlPattern);
	}

	/**
	 * 
	 * @param subUrl
	 * @return servlet path
	 */
	public String match(String subUrl) {
		log.debug("mathing.pattern type:"+type+". pattern:"+urlPattern+". input url:"+subUrl);
		String result = null;
		//TODO to support favicon
		if (subUrl == null || !subUrl.startsWith("/"))
			return null;
//			throw new IllegalArgumentException(url);
		if (type == UrlPatternType.EXACT) {
			if (subUrl.equals(urlPattern))
				result = subUrl;
		} else if (type == UrlPatternType.PATH) {
			String prefix = urlPattern.substring(0, urlPattern.length() - 2);
			if (subUrl.startsWith(prefix)) {
				result = prefix;
			}
		}
		else if(type==UrlPatternType.SUFFIX){
			String suffix=urlPattern.substring(1);
			if(subUrl.endsWith(suffix))
				result=subUrl;
		}
		else if(type==UrlPatternType.ROOT)
		{
			if(subUrl.equals("/"))
				result="";
		}
		else if(type==UrlPatternType.DEFAULT)
			return subUrl;
		return result;
	}
	
	public UrlPatternType getType() {
		return type;
	}
	
	public String getUrlPattern() {
		return urlPattern;
	}

	public int getLength(){
		return urlPattern.length();
	}

	@Override
	public int compareTo(UrlPattern o) {
		int rs=o.getType().getPriority()-this.getType().getPriority();
		if(rs!=0)
			return rs;
		return o.getLength()-this.getLength();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof UrlPattern))
			return false;
		UrlPattern p1=(UrlPattern)obj;
		if(this.getUrlPattern().equals(p1.getUrlPattern()))
			return true;
		return false;
	}
}
