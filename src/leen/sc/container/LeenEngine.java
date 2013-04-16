package leen.sc.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.servlet.ServletException;

import leen.sc.ConfigException;
import leen.sc.ServletMapExcepion;
import leen.sc.filter.LeenFilterManager;
import leen.sc.request.LeenRequest;
import leen.sc.response.LeenResponse;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.util.ResponseMessages;

import org.apache.log4j.Logger;

public class LeenEngine implements Container {

	public final static String VERSION = "1.0";
	private static final Logger log = Logger.getLogger(LeenEngine.class);
	private List<ILeenContext> contexts = new ArrayList<ILeenContext>();
	private ILeenContext defaultContext;
	private String path;
	private Executor executor;

	public LeenEngine(String path) throws ConfigException {
		this.path = path;
	}

	public void init() throws ConfigException {
		initContexts();
	}

	private void initContexts() throws ConfigException {
		log.info("creating engine leen V" + VERSION);
		File webappRoot = new File(path);
		File[] webapps = webappRoot.listFiles();
		for (File webapp : webapps) {
			if (webapp.isDirectory()) {
				ILeenContext ctx = null;
				if (webapp.getName().equals("ROOT")) {
					ctx = new LeenContext(this.getPath(), "/");
					defaultContext = ctx;
				} else {
					ctx = new LeenContext(this.getPath(), "/"
							+ webapp.getName());
					contexts.add(ctx);
				}
				ctx.setExecutor(executor);
				ctx.setFilterManager(new LeenFilterManager());
				ctx.setSWM(new ServletWrapperManager());
				ctx.setWelcomeManager(new WelcomeManager());
				ctx.init();
			}
		}
	}

	public void leen(LeenRequest request, LeenResponse response) {
		try {
			mapContext(request.getRequestURI()).request(request, response);
		} catch (ServletMapExcepion e) {
			e.printStackTrace();
			try {
				response.setStatus(404);
				response.getWriter().write(
						ResponseMessages.notFound(request.getRequestURI()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ServletException e) {
			e.printStackTrace();
			try {
				response.setStatus(500);
				response.getWriter().write(ResponseMessages.exception(e));
			} catch (IOException e1) {
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				response.setStatus(500);
				response.getWriter().write(ResponseMessages.exception(e));
			} catch (IOException e1) {
			}
		}

	}

	public ILeenContext mapContext(String uri) throws ServletMapExcepion {
		int idx1 = uri.indexOf("/");
		int idx2 = uri.indexOf("/", idx1 + 1);
		String contextPath = null;
		if (idx2 == -1)
			contextPath = uri.substring(idx1);
		else
			contextPath = uri.substring(idx1, idx2);
		for (ILeenContext context : contexts) {
			if (context.getContextPath().equals(contextPath))
				return context;
		}
		if (defaultContext == null)
			throw new ServletMapExcepion("failt to map context for '"+uri+"'");
		return defaultContext;
	}

	public String getPath() {
		return path;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
}
