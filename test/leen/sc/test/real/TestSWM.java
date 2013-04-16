package leen.sc.test.real;


import junit.framework.TestCase;
import leen.sc.container.LeenContext;
import leen.sc.servlet.ServletWrapperManager;
import leen.sc.test.real.util.MockContextFactory;

import org.junit.Before;

public class TestSWM extends TestCase{

	private ServletWrapperManager swm;
	
	@Before
	public void setUp() throws Exception {
		LeenContext context=MockContextFactory.getInstance();
		swm=new ServletWrapperManager();	
		context.setSWM(swm);
		context.init();
	}
	
	public void testExist(){
		assertTrue(swm.exist("/test2"));
		assertTrue(swm.exist("/leen/test1"));
	}

}
