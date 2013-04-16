package leen.sc.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.FileTypeMap;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;

import leen.sc.ConfigException;
import leen.sc.filter.LeenFilterWrapper;
import leen.sc.filter.ServletNameFilterMappingData;
import leen.sc.filter.UrlPatternFilterMappingData;
import leen.sc.jsp.JspProcessor;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.staticresource.StaticResourceProcessor;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class WebXmlParser {
	private static Logger log = Logger.getLogger(WebXmlParser.class);

	public void parse(ILeenContext context, File file) throws ConfigException {
		try {
			Document doc = getDoc(file);
			parseServlets(context, doc);
			parseListeners(context, doc);
			parseWelcome(context, doc);
			parseFilters(context, doc);
		} catch (DocumentException e) {
			throw new ConfigException();
		} catch (IOException e) {
			log.error("error reading web.xml for context "
					+ context.getContextPath());
		}
	}

	private Document getDoc(File file) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Map<String, String> ns = new HashMap<String, String>();
		ns.put("javaee", "http://java.sun.com/xml/ns/javaee");
		saxReader.getDocumentFactory().setXPathNamespaceURIs(ns);
		return saxReader.read(file);
	}

	private void parseWelcome(ILeenContext context, Document doc) {
		log.debug("start parsing welcome for context "
				+ context.getContextPath());
		WelcomeManager welcomeMgr = context.getWelcomeManager();
		Node node = doc.selectSingleNode("/web-app/javaee:welcome-file-list");
		if (node == null)
			return;
		List<Node> welcomeNodeList = node.selectNodes("javaee:welcome-file");
		for (Node welcomeNode : welcomeNodeList) {
			String welcomeFileName = welcomeNode.getText();
			log.debug("parse file name " + welcomeFileName);
			welcomeMgr.addWelcome(welcomeFileName);
		}
	}

	private void parseServlets(ILeenContext context, Document doc)
			throws IOException, ConfigException {
		registerSysServlets(context);

		List<Node> list = doc.selectNodes("/web-app/javaee:servlet");
		for (Node node : list) {
			String servletName = node.selectSingleNode("javaee:servlet-name")
					.getText();
			String className = node.selectSingleNode("javaee:servlet-class")
					.getText();
			ServletRegistration reg = context
					.addServlet(servletName, className);
			if (reg == null)
				continue;
			List<Node> mappingNodes = doc
					.selectNodes("/web-app/javaee:servlet-mapping[javaee:servlet-name='"
							+ servletName + "']");
			for (Node mappingNode : mappingNodes) {
				List<Node> urlPatterns = mappingNode
						.selectNodes("javaee:url-pattern");
				for (Node urlPattern : urlPatterns) {
					log.debug("add mapping for servlet " + servletName
							+ ",url-pattern:" + urlPattern.getText());
					reg.addMapping(urlPattern.getText());
				}
			}
			List<Node> initParams = node.selectNodes("javaee:init-param");
			for (Node initParam : initParams) {
				String paramName = initParam.selectSingleNode(
						"javaee:param-name").getText();
				String paramValue = initParam.selectSingleNode(
						"javaee:param-value").getText();
				reg.setInitParameter(paramName, paramValue);
			}
		}
	}

	private void registerSysServlets(ILeenContext context)
			throws ConfigException {
		try {
			context.addServlet("staticResourceProcessor",
					StaticResourceProcessor.class).addMapping("/");
			context.addServlet("jspProcessor", JspProcessor.class).addMapping(
					"*.jsp");
		} catch (NullPointerException e) {
			throw new ConfigException();
		}

	}

	private void parseListeners(ILeenContext context, Document doc) {
		List<Node> nodes = doc
				.selectNodes("/web-app/javaee:listener/javaee:listener-class");
		for (Node node : nodes) {
			String listenerClass = node.getText().trim();
			log.info("adding listener " + listenerClass + " for context "
					+ context.getContextPath());
			context.addListener(listenerClass);
		}
	}

	private void parseFilters(ILeenContext context, Document doc) {
		List<Node> nodes = doc.selectNodes("/web-app/javaee:filter");
		List<LeenFilterWrapper> filterWrapperList = new ArrayList<LeenFilterWrapper>();
		String filterClass = null;
		for (Node node : nodes) {
			try {
				String filterName = node.selectSingleNode("javaee:filter-name")
						.getText();
				log.info("creating filter " + filterName + " on context "
						+ context.getContextPath());

				filterClass = node.selectSingleNode("javaee:filter-class")
						.getText();

				Filter filter = (Filter) context.getClassLoader()
						.loadClass(filterClass).newInstance();
				FilterRegistration reg = context.addFilter(filterName, filter);
				List<Node> initParams = node.selectNodes("javaee:init-param");
				Map<String, String> initParameters = new HashMap<String, String>();
				for (Node initParam : initParams) {
					String paramName = initParam.selectSingleNode(
							"javaee:param-name").getText();
					String paramValue = initParam.selectSingleNode(
							"javaee:param-value").getText();
					reg.setInitParameter(paramName, paramValue);
				}
				List<ServletNameFilterMappingData> mappingForServletName = new ArrayList<ServletNameFilterMappingData>();
				List<UrlPatternFilterMappingData> mappingForUrlPattern = new ArrayList<UrlPatternFilterMappingData>();
				List<Node> filterMappings = doc
						.selectNodes("/web-app/javaee:filter-mapping[javaee:filter-name='"
								+ filterName + "']");
				for (Node filterMapping : filterMappings) {
					List<Node> servletNames = filterMapping
							.selectNodes("javaee:servlet-name");
					List<Node> dispatchers = filterMapping
							.selectNodes("javaee:dispatcher");
					EnumSet<DispatcherType> dispatcherTypes = EnumSet
							.noneOf(DispatcherType.class);

					for (Node dispatcher : dispatchers) {
						DispatcherType dispatcherType = DispatcherType
								.valueOf(dispatcher.getText());
						// TODO check illegal value
						dispatcherTypes.add(dispatcherType);
					}
					if (dispatcherTypes.isEmpty())
						dispatcherTypes.add(DispatcherType.REQUEST);
					for (Node servletName : servletNames)
						reg.addMappingForServletNames(dispatcherTypes, true,
								servletName.getText());
					List<Node> urlPatterns = filterMapping
							.selectNodes("url-pattern");
					for (Node urlPattern : urlPatterns)
						reg.addMappingForUrlPatterns(dispatcherTypes, true,
								urlPattern.getText());

				}
				// context.addFilterWrapper(wrapper);
			} catch (ClassNotFoundException e) {
				log.warn("filter class not found " + filterClass
						+ "for context " + context.getContextPath());
			} catch (InstantiationException e) {
				log.warn("filter class " + filterClass
						+ " can't be instantiated for context "
						+ context.getContextPath());
			} catch (IllegalAccessException e) {
				log.warn("filter class " + filterClass
						+ " can't be accessed for context "
						+ context.getContextPath());
			}
		}
	}
}
