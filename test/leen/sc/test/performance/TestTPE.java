package leen.sc.test.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Before;

public class TestTPE extends TestCase{
	private ThreadPoolExecutor tpe;
	private static int POOL_SIZE = 100;
	private static int WAINT_QUEUE = POOL_SIZE*3;

	@Before
	public void setUp() throws Exception {
		tpe = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(WAINT_QUEUE));
	}

	public void testAverage() throws Exception {
		int taskCount=POOL_SIZE*4;
		List<Task> taskList=new ArrayList<Task>();
		for(int i=0;i<taskCount;i++)
			taskList.add(new Task(System.currentTimeMillis()));
		
		for (Task task:taskList)
			tpe.execute(task);
		Thread.sleep(2000);
		long sum=0;
		for (Task task:taskList)
			sum+=task.getTime();
		long average=sum/taskCount;
		System.out.println("average:"+average+"ms");
	}

	class Task implements Runnable {
		private long start;
		private long time;
		
		public Task(long start) {
			this.start = start;
		}

		public long getTime() {
			return time;
		}



		@Override
		public void run() {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			time=System.currentTimeMillis() - start;
		}

	}

}
