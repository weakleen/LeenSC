package leen.sc.startup;

import java.lang.reflect.Method;

import leen.sc.classloader.LeenCLFactory;
import leen.sc.startup.util.CommonInfo;

public class LeenBootstrap {
	public static void main(String[] args) throws ClassNotFoundException {
		if(args.length>0)
			CommonInfo.LEEN_BASE=args[0];
		else
			CommonInfo.LEEN_BASE = System.getProperty("user.dir");
		ClassLoader rootClassLoader = LeenCLFactory.getRoot();
		Thread.currentThread().setContextClassLoader(rootClassLoader);
		try {
			Object leen = rootClassLoader.loadClass("leen.sc.Leen")
					.newInstance();
			Method method = leen.getClass().getMethod("start",new Class[]{ClassLoader.class});
			method.invoke(leen,rootClassLoader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
