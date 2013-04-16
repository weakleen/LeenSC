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
		// ��ȡbufferʱ������޿���buffer�һ����δ�����򴴽�buffer
		for (int i = 1; i <= 10; i++) {
			assertEquals(buffersize, pool.getBuffer().length);
			assertEquals(i, pool.getBufferCount());
			assertTrue(pool.getIdleBufferCount() == 0);
		}
		// ��ȡbufferʱ������޿���buffer����������������׳�BufferPoolException
		try {
			pool.getBuffer();
			fail();
		} catch (BufferPoolException e) {
		}
	}
	
	public void testReturnBuffer(){
		
		byte[] buffer1=pool.getBuffer();
		byte[] buffer2=new byte[1024];
//		�������ػ���
		pool.returnBuffer(buffer1);
		assertEquals(1,pool.getIdleBufferCount());
//		���ط�pool�����Ļ��壬Ӧ���׳��쳣
		try{
			pool.returnBuffer(buffer2);
			fail();
		}
		catch (BufferPoolException e) {
		}
	}

}
