package leen.sc.response;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BufferPool implements BufferManager {
	private List<byte[]> bufferList = Collections
			.synchronizedList(new LinkedList<byte[]>());
	private LinkedList<byte[]> idleBufferList = new LinkedList<byte[]>();
	private static final int DEFAULT_POOL_SIZE = 200;
	private int poolsize = DEFAULT_POOL_SIZE;
	private static final int DEFAULT_BUFFER_SIZE = 200;
	private int buffersize = DEFAULT_BUFFER_SIZE;

	public BufferPool() {
	}

	public BufferPool(int poolsize, int buffersize) {
		this.poolsize = poolsize;
		this.buffersize = buffersize;
	}

	/* (non-Javadoc)
	 * @see leen.sc.response.BufferManager#getBuffer()
	 */
	@Override
	public byte[] getBuffer() {
		synchronized (idleBufferList) {
			if (idleBufferList.isEmpty())
				return createBuffer();
			synchronized (idleBufferList) {
				return idleBufferList.pop();
			}
		}
	}

	/* (non-Javadoc)
	 * @see leen.sc.response.BufferManager#returnBuffer(byte[])
	 */
	@Override
	public void returnBuffer(byte[] toReturn) {
		if (!bufferList.contains(toReturn))
			throw new BufferPoolException();
		synchronized (idleBufferList) {
			idleBufferList.push(toReturn);
		}
	}

	public int getIdleBufferCount() {
		return idleBufferList.size();
	}

	public int getPoolsize() {
		return poolsize;
	}

	public int getBufferCount() {
		return bufferList.size();
	}

	public void destroy() {
		synchronized (this) {
			if (bufferList != null && idleBufferList != null) {
				bufferList.clear();
				idleBufferList.clear();
				bufferList = null;
				idleBufferList = null;
			}
		}
	}

	private byte[] createBuffer() {
		if (bufferList.size() < poolsize) {
			synchronized (bufferList) {
				if (bufferList.size() < poolsize) {
					byte[] buffer = new byte[buffersize];
					bufferList.add(buffer);
					return buffer;
				} else
					throw new BufferPoolException();
			}
		} else
			throw new BufferPoolException();
	}

}
