package leen.sc.response;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import leen.sc.container.ClientAbortException;

public class LeenServletOutputStream extends ServletOutputStream {
	private OutputStream out;
	private int bufferSize = 10240;
	private byte[] buffer;
	private int pos = 0;
	private LeenResponse response;
	private BufferManager bufferPool;

	private boolean isClosed;

	public LeenServletOutputStream(BufferManager bufferPool, byte[] buffer,
			OutputStream originOut) {
		this.buffer = buffer;
		this.bufferPool = bufferPool;
		this.out = originOut;
	}

	public void setResponse(LeenResponse response) {
		this.response = response;
	}

	@Override
	public void write(int b) throws IOException {
		if (isClosed)
			return;
		if (response.isClosed())
			return;
		int newpos = pos + 1;
		if (newpos >= buffer.length)
			flush();
		buffer[pos++] = (byte) b;
	}

	public void setBuffersize(int buffersize) {
		if (isClosed)
			return;
		this.bufferSize = buffersize;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		if (isClosed)
			return;
		this.bufferSize = bufferSize;
	}

	public void reset() {
		if (isClosed)
			return;
		pos = 0;
	}

	public void prepare(byte[] preparemsg) throws IOException {
		if (!response.isCommitted())
			out.write(preparemsg);
		else
			throw new IllegalStateException();
	}

	@Override
	public void flush() throws IOException {
		if (isClosed)
			return;
		try {
			if (response.isClosed())
				return;
			if (!response.isCommitted()) {
				response.prepareResponse();
				response.getStateMachine().commit();
			}
			out.write(buffer, 0, pos);
			pos = 0;
		} catch (IOException e) {
			throw new ClientAbortException();
		}
	}

	@Override
	public void close() throws IOException {
		if (isClosed)
			return;
		isClosed = true;
		super.close();
		out.close();
		bufferPool.returnBuffer(buffer);
	}
}
