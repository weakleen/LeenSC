package leen.sc.test.real;

import junit.framework.TestCase;
import leen.sc.response.BufferPool;
import leen.sc.response.BufferPoolException;

import org.junit.Before;

public class TestBufferPool extends TestCase {
	int poolsize = 10;
	int buffersize = 10;
	BufferPool pool;
	
	@Before
	public void setUp() throws Exception {
		pool = new BufferPool(poolsize, buffersize);
	}

	public void testGetBuffer() {
		
		assertTrue(pool.getBufferCount() == 0);
		// 获取buffer时，如果无空闲buffer且缓冲池未满，则创建buffer
		for (int i = 1; i <= 10; i++) {
			assertEquals(buffersize, pool.getBuffer().length);
			assertEquals(i, pool.getBufferCount());
			assertTrue(pool.getIdleBufferCount() == 0);
		}
		// 获取buffer时，如果无空闲buffer但缓冲池已满，则抛出BufferPoolException
		try {
			pool.getBuffer();
			fail();
		} catch (BufferPoolException e) {
		}
	}
	
	public void testReturnBuffer(){
		
		byte[] buffer1=pool.getBuffer();
		byte[] buffer2=new byte[1024];
//		正常返回缓冲
		pool.returnBuffer(buffer1);
		assertEquals(1,pool.getIdleBufferCount());
//		返回非pool创建的缓冲，应该抛出异常
		try{
			pool.returnBuffer(buffer2);
			fail();
		}
		catch (BufferPoolException e) {
		}
	}

}
