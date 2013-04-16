package leen.sc.staticresource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import leen.sc.request.LeenRequest;
import leen.sc.util.ResponseMessages;

public class StaticResourceProcessor extends HttpServlet {
	private static Logger log = Logger.getLogger(StaticResourceProcessor.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		OutputStream out = null;
		File resourceFile = null;
		InputStream resourceInput = null;
		try {
			String encoding = req.getCharacterEncoding();
			encoding = encoding == null ? System.getProperty("file.encoding")
					: encoding;
			log.debug("encoding:" + req.getCharacterEncoding());
			String resourceName = URLDecoder.decode(((LeenRequest)req).getCurrentURI().getSubUrl(),
					encoding);
			String realPath = req.getServletContext().getRealPath(resourceName);
			resourceFile = new File(realPath);
			if (!resourceFile.exists())
				throw new FileNotFoundException();
			String ct = getContentType(resourceFile);
			resourceInput = new FileInputStream(realPath);
			if (ct != null)
				resp.setContentType(ct);
			out = resp.getOutputStream();
			ByteBuffer buffer = ByteBuffer.allocate(256);
			int n = 0;
			while ((n = resourceInput.read(buffer.array())) != -1){
				out.write(buffer.array(), 0, n);
			}
			resp.flushBuffer();
		} catch (FileNotFoundException e) {
			out = resp.getOutputStream();
			resp.setStatus(404);
			out.write(ResponseMessages.notFound(req.getRequestURI()).getBytes());
		} finally {
			if (resourceInput != null)
				resourceInput.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	private String getContentType(File file) throws FileNotFoundException {
		try {
			return URLConnection.getFileNameMap().getContentTypeFor(
					file.toURI().toURL().getPath());
		} catch (MalformedURLException e) {
			throw new FileNotFoundException();
		}
	}
}
