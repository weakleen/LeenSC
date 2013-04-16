package leen.sc.test.performance;

import javax.servlet.http.Cookie;

import leen.sc.request.RequestURI;
import leen.sc.test.performance.util.TestClass1;

public class RequestURICreatingTime {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long time = 0;
		new RequestURI();
		time = System.currentTimeMillis();
		for (long i = 0; i < 10000; i++)
			new RequestURI();
		time = System.currentTimeMillis() - time;
		System.out.println("RequestURI creating time:" + time + "ms");

		time = System.currentTimeMillis();
		for (long i = 0; i < 10000; i++)
			new TestClass1();
		time = System.currentTimeMillis() - time;
		System.out.println("TestClass1 creating time:" + time + "ms");
		
		time = System.currentTimeMillis();
		for (long i = 0; i < 10000; i++){
			Cookie[] cookies=new Cookie[2];
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Cookie[] creating time:" + time + "ms");
		
	}

}
