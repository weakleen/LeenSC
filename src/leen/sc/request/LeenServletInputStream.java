package leen.sc.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletInputStream;

import org.apache.log4j.Logger;

public class LeenServletInputStream extends ServletInputStream {

	private static Logger log = Logger.getLogger(LeenServletInputStream.class);

	private InputStream originInput;
	private int contentLength = -1;
	private int position = 0;
	private boolean error;
	private boolean startReadBody;
	private List<Integer> list = new LinkedList<Integer>();

	public LeenServletInputStream(InputStream originInput) {
		this.originInput = originInput;
	}

	@Override
	public int read() throws IOException {
		int currentByte;
		if (!startReadBody)
			currentByte = boforeReadBody();
		else if (contentLength != -1)
			currentByte = readBody();
		else
			return -1;
		if (!startReadBody)
			assetStartReadBody(currentByte);
		return currentByte;
	}

	private void assetStartReadBody(int b) {
		if (b == '\n' || b == '\r')
			list.add(b);
		else
			list.clear();
		if (list.size() == 2) {
			if (list.get(0).equals(list.get(1)))
				startReadBody = true;
		}
		if (list.size() == 4) {
			if (list.get(0).equals(list.get(2))
					&& list.get(1).equals(list.get(3)))
				startReadBody = true;
		}
	}

	private int boforeReadBody() throws IOException {
		return originInput.read();
	}

	private int readBody() throws IOException {
		if (contentLength == -1)
			throw new IllegalStateException();
		if (error)
			throw new IOException();
		if (position == contentLength)
			return -1;
		int b = originInput.read();
		position++;
		if (b == -1 && position < contentLength - 1) {
			error = true;
			throw new IOException();
		}
		return b;
	}

	public boolean isStartReadBody() {
		return startReadBody;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	@Override
	public int available() throws IOException {
		if (!startReadBody)
			return originInput.available();
		else {
			return contentLength - position;
		}
	}
}
