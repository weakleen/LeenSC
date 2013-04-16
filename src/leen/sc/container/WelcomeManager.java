package leen.sc.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import leen.sc.ServletMapExcepion;
import leen.sc.request.LeenRequest;
import leen.sc.request.RequestURI;
import leen.sc.response.LeenResponse;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.startup.util.CommonInfo;
import leen.sc.util.ResponseMessages;

import org.apache.log4j.Logger;

public class WelcomeManager {

	private static Logger log = Logger.getLogger(WelcomeManager.class);

	private ILeenContext context;
	private List<String> staticResource = new ArrayList<String>();
	private List<String> dynamicResource = new ArrayList<String>();
	private ServletWrapperManager swm;

	// states
	public static final int NORMAL = 0;
	public static final int NOT_DIRECTORY = 1;
	public static final int NOT_END_WITH_SLASH = 2;
	public static final int NOT_FOUND = 3;

	public void setSWM(ServletWrapperManager swm) {
		this.swm = swm;
	}

	public void setContext(ILeenContext context) {
		this.context = context;
	}

	public void addWelcome(String fileName) {
		if(fileName==null)
			throw new IllegalArgumentException("file name cann't be null");
		String[] extArray = CommonInfo.STATIC_EXTS;
		for (String ext : extArray) {
			if (fileName.endsWith(ext)) {
				staticResource.add(fileName);
				log.debug("add static file "+fileName+" for context "+context.getContextPath());
				return;
			}
		}
		dynamicResource.add(fileName);
		log.debug("add dynamic file "+fileName+" for context "+context.getContextPath());
	}

	public void welcome(LeenRequest request, LeenResponse response)
			throws IOException, ServletException, ServletMapExcepion {
		log.debug("start welcome for context "+context.getContextPath());
		StringBuilder uri = new StringBuilder();
		int result = welcome0(request.getCurrentURI(), uri);
		log.debug("welcome result "+result);
		switch (result) {
		case NOT_DIRECTORY:
			throw new ServletMapExcepion();
		case NOT_END_WITH_SLASH:
			response.sendRedirect(request.getSubUrl() + "/");
			break;
		case NOT_FOUND:
			throw new ServletMapExcepion();
		case NORMAL:
			request.getRequestDispatcher(uri.toString())
					.forward(request.getWrapper(), response.getWrapper());
		}

	}

	public int welcome0(RequestURI originUri, StringBuilder outUri) {
		String path = "";
		log.debug("oring uri "+originUri.getURI());
		// directory entry 判断
		if (!originUri.getURI().equals(context.getContextPath())) {
			path = originUri.getSubUrl();
			File file = new File(context.getRealPath(path));
			if (!file.isDirectory())
				return NOT_DIRECTORY;
		}

		// (2.)斜杠处理
		if (!path.endsWith("/")) {
			return NOT_END_WITH_SLASH;
		}

		// 选择资源
		String uri = null;
		for (String resource : staticResource) {
			String resourcePath = context.getRealPath(path + resource);
			File file = new File(resourcePath);
			if (file.exists() && file.isFile()) {
				log.debug("static welcome file found :"
						+ file.getAbsolutePath());
				uri = originUri.getSubUrl() + resource;
				break;
			}
		}

		if (uri == null) {
			for (String resource : dynamicResource) {
				if (swm.exist(originUri.getSubUrl() + resource))
					uri = originUri.getSubUrl() + resource;
			}
		}

		// not found
		if (uri == null)
			return NOT_FOUND;

		outUri.append(uri);
		return NORMAL;
	}
}
