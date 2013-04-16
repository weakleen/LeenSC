package leen.sc.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import leen.sc.Processor;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;

import org.apache.log4j.Logger;

public class Connector {

	private static Logger log = Logger.getLogger(Connector.class);

	private int port;
	private int timeout;
	private ServerSocket serverSocket;
	private boolean stopped;
	private Processor processor;
	private Executor executor;
	private long rejectedCount = 0;

	public Connector() {

	}

	public void init() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		while (!stopped) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				executor.execute(new Worker(socket, System.currentTimeMillis(),
						(ThreadPoolExecutor) executor));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RejectedExecutionException e) {
				try {
					log.warn("server busy,request rejected,total rejected count:"
							+ rejectedCount);
					socket.close();
				} catch (IOException e1) {
				}
			}

		}
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	private static List<Integer> executeTimeList = new ArrayList<Integer>();
	private static List<Integer> concurrenceList = new ArrayList<Integer>();

	class Worker extends Thread {
		private Socket socket;
		private long start;
		private ThreadPoolExecutor TPE;
		private boolean logPerformance;

		public Worker(Socket socket, long start) {
			this.socket = socket;
			this.start = start;
		}

		public Worker(Socket socket, long start, ThreadPoolExecutor TPE) {
			this.socket = socket;
			this.start = start;
			this.TPE = TPE;
			this.logPerformance = true;
		}

		@Override
		public void run() {
			int concurrence = 0;
			try {
				socket.setSoTimeout(timeout);
				processor.service(socket);
				// probably socketTimeoutException
			} catch (IOException e) {
				try {
					if (e instanceof SocketTimeoutException)
						log.debug("socket time out");
					socket.close();
				} catch (IOException e1) {
				}
			}
			if (logPerformance) {
				long executeTime = System.currentTimeMillis() - start;
				log.debug("request finished in " + executeTime
						+ "ms,concurrence:" + concurrence);
			}
		}
	}

}
