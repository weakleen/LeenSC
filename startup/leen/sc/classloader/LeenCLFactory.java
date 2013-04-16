package leen.sc.classloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leen.sc.startup.util.CommonInfo;

public class LeenCLFactory {
	private static URLClassLoader rootCL;
	private static Object rootLock=new Object();
	private static Map<String, ClassLoader> contextCLs = new HashMap<String, ClassLoader>();
	
	public static ClassLoader getRoot() {
		try {
			if (rootCL != null)
				return rootCL;
			synchronized (rootLock) {
				if (rootCL != null)
					return rootCL;
				List<URL> urlList = new ArrayList<URL>();
				File baseLib = new File(CommonInfo.LEEN_BASE + File.separator
						+ CommonInfo.LIB);
				urlList.add(baseLib.toURI().toURL());
				genJarUrls(baseLib, urlList);
				rootCL = new URLClassLoader(urlList.toArray(new URL[0]));
				return rootCL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error();
		}
	}

	public static ClassLoader getContext(String contextRealPath) {
		try {
			if (contextCLs.containsKey(contextRealPath))
				return contextCLs.get(contextRealPath);
			synchronized (contextCLs) {
				if (contextCLs.containsKey(contextRealPath))
					return contextCLs.get(contextRealPath);
				File contextLib = new File(contextRealPath + CommonInfo.WEB_INF
						+ CommonInfo.LIB);
				File contextClasses = new File(contextRealPath
						+ CommonInfo.WEB_INF + CommonInfo.CLASSES);
				List<URL> urlList = new ArrayList<URL>();
				urlList.add(contextLib.toURI().toURL());
				urlList.add(contextClasses.toURI().toURL());
				genJarUrls(contextLib, urlList);
				ClassLoader loader = new URLClassLoader(
						urlList.toArray(new URL[0]), getRoot());
				contextCLs.put(contextRealPath, loader);
				return loader;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error();
		}

	}

	private static void genJarUrls(File baseLib, List<URL> urlList)
			throws MalformedURLException, IOException {
		File[] fileList = baseLib.listFiles();
		if (fileList == null)
			return;
		for (File file : fileList) {
			if (file.getName().endsWith(".jar")) {
				urlList.add(file.toURI().toURL());
			}
		}
	}
}
