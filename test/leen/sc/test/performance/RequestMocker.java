package leen.sc.test.performance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import leen.sc.ConfigException;
import leen.sc.Processor;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.BufferPool;

public class RequestMocker {

	public static void main(String[] args) {
		
		long time=System.currentTimeMillis();
		
		byte[] requestMsg = ("GET /Mock/test HTTP/1.1\r\n"
				+ "host: localhost\r\n\r\n").getBytes();

		ByteArrayInputStream input = new ByteArrayInputStream(requestMsg);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Processor p = null;
		try {
			p = new Processor();

			int maxThreads = 200;
			int queueLength = 200;
			int buffersize = 1024 * 8;
			p.setBufferManager(new BufferPool(maxThreads, buffersize));

			Executor executor = new ThreadPoolExecutor(maxThreads, maxThreads,
					0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
							queueLength));
			p.setExecutor(executor);
			p.start();
			for (int i = 0; i < 10000; i++){
				input.reset();
				output.reset();
				p.service(input, output);
			}
			p.stop();
			time=System.currentTimeMillis()-time;
			System.out.println("execute time: "+time+"ms");
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
