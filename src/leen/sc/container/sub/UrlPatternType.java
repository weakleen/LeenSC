package leen.sc.container.sub;

import java.util.regex.Pattern;

import leen.sc.container.MalFormedUrlPatternException;

public enum UrlPatternType {
	EXACT(3),PATH(2),SUFFIX(1),ROOT(0),DEFAULT(-1);
	
	private static final Pattern EXACT_PATTERN=Pattern.compile("/[^\\*]+"); 
	private static final Pattern PATH_PATTERN=Pattern.compile("/([^\\*]+/)*\\*");
	private static final Pattern SUFFIX_PATTERN=Pattern.compile("\\*\\.[^\\*]+");
	private static final Pattern DEFAULT_PATTERN=Pattern.compile("/");
	private static final Pattern ROOT_PATTERN=Pattern.compile("");
	
	private int priority;
	
	private UrlPatternType(int priority){
		this.priority=priority;
	}
	
	public static UrlPatternType match(String urlPattern) throws MalFormedUrlPatternException{
		if(EXACT_PATTERN.matcher(urlPattern).matches())
			return EXACT;
		if(PATH_PATTERN.matcher(urlPattern).matches())
			return PATH;
		if(SUFFIX_PATTERN.matcher(urlPattern).matches())
			return SUFFIX;
		if(DEFAULT_PATTERN.matcher(urlPattern).matches())
			return  DEFAULT;
		if(ROOT_PATTERN.matcher(urlPattern).matches())
			return  ROOT;
		throw new MalFormedUrlPatternException();
	}

	public int getPriority() {
		return priority;
	}
	
}
