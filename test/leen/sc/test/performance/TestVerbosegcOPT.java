package leen.sc.test.performance;

public class TestVerbosegcOPT {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int size = 5*1024 * 1024;
		for (int i = 0; i < 1000; i++) {
			byte[] b1 = new byte[size];
		}
/*		System.out.println("start synchronous gc");
		long time = System.currentTimeMillis();
		System.gc();
		time = System.currentTimeMillis() - time;
		System.out.println("gc time:" + time + "ms");*/
	}

}
