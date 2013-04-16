package leen.sc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executor;

import leen.sc.container.LeenEngine;
import leen.sc.request.LeenRequest;
import leen.sc.response.BufferManager;
import leen.sc.response.BufferPoolException;
import leen.sc.response.LeenResponse;
import leen.sc.response.LeenServletOutputStream;
import leen.sc.session.SessionManager;
import leen.sc.startup.util.CommonInfo;

import org.apache.log4j.Logger;

public class Processor {
	private LeenEngine engine;
	private SessionManager sessionManager;
	private Executor executor;
	private BufferManager bufferPool;

	private static Logger log = Logger.getLogger(Processor.class);

	public Processor() throws ConfigException {
		sessionManager = new SessionManager();
		engine = new LeenEngine(CommonInfo.LEEN_BASE + File.separator
				+ "webapps");
	}

	public void start() throws ConfigException {
		engine.setExecutor(executor);
		engine.init();
		sessionManager.start();
	}

	public void stop() {
		sessionManager.stop();
	}

	public LeenServletOutputStream createServletOut(OutputStream originOut) {
		try {
			LeenServletOutputStream out = new LeenServletOutputStream(
					bufferPool, bufferPool.getBuffer(), originOut);
			return out;
		} catch (BufferPoolException e) {
			log.error("fatal error,pool full");
			throw new Error();
		}
	}

	public void service(Socket socket) throws IOException {
		InetInfo inetInfo=new InetInfo(socket.getLocalAddress(), socket.getInetAddress(), socket.getLocalPort(),socket.getPort());
		service(socket.getInputStream(), socket.getOutputStream(),inetInfo);
	}

	public void service(InputStream input, OutputStream output,InetInfo inetInfo)
			throws IOException {
		LeenRequest request = null;
		LeenResponse response = null;
		try {
			request = new LeenRequest(input);
			request.setInetInfo(inetInfo);
			response = new LeenResponse(createServletOut(output));

			response.setRequest(request);
			request.setSessionManager(sessionManager);
			request.setResponse(response);
			request.parse();
			log.debug("get request to " + request.getRequestURI());
			engine.leen(request, response);
			if (!request.isAsyncStarted()) {
				log.debug("finish response in connector");
				if (!response.isFinished())
					response.finish();
			} else
				log.debug("async started");
			log.debug("response finished:" + response.isFinished());
		} catch (RequestException e) {
			e.printStackTrace();
			response.setStatus(400);
		}
	}

	public void sendClientError(LeenResponse response, int code, String msg)
			throws IOException {
		response.setStatus(400, "bad request");
		response.prepareResponse();
		PrintWriter writer = response.getWriter();
		writer.write("ERROR " + code);
		writer.flush();
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void setBufferManager(BufferManager bufferPool) {
		this.bufferPool = bufferPool;
	}
}
