package leen.sc.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentType {
	private String encoding;
	private String contentType;
	private final static String regex = "charset\\s*=\\s*([\\w-]+)";
	private final static Pattern ptn = Pattern.compile(regex);

	public void setContentType(String contentType) {
		this.contentType = contentType;
		Matcher mt = ptn.matcher(contentType);
		if (mt.find()){
			encoding = mt.group(1);
		}
		else{
			if(encoding!=null)
				this.contentType+=(";charset="+encoding);
		}
	}

	public String getCharacterEncoding() {
		return encoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setCharaterEncoding(String encoding) {
		if (contentType != null) {
			if (encoding != null)
				contentType.replaceFirst(regex, "charset=" + encoding);
			else
				contentType += ("charset=" + encoding);
		}
		this.encoding = encoding;

	}

	public String toDefault() {
		setContentType("text/html;charset="
				+ System.getProperty("file.encoding"));
		return contentType;
	}

	public boolean isSet() {
		return contentType != null;
	}

}
