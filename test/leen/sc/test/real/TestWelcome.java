package leen.sc.test.real;


import junit.framework.TestCase;
import leen.sc.container.ILeenContext;
import leen.sc.container.WelcomeManager;
import leen.sc.request.RequestURI;
import leen.sc.test.real.util.MockContextFactory;

import org.junit.Before;

public class TestWelcome extends TestCase{

	private WelcomeManager welcomeManager;
	
	@Before
	public void setUp() throws Exception {
		welcomeManager=new WelcomeManager();
		ILeenContext context=MockContextFactory.getInstance();
		context.setWelcomeManager(welcomeManager);
		welcomeManager.addWelcome("test1");
		welcomeManager.addWelcome("index.html");
		context.init();
	}
	
	public void testAssertDirectory(){
		StringBuilder outUri=new StringBuilder();
		RequestURI originUri=new RequestURI();
		originUri.setRawURI("/Mock/abc/");
		originUri.setContextPath("/Mock");
		int rs=welcomeManager.welcome0(originUri, outUri);
		assertEquals(WelcomeManager.NOT_DIRECTORY,rs);
	}
	
	public void testAssertSlash(){
		StringBuilder outUri=new StringBuilder();
		RequestURI originUri=new RequestURI();
		originUri.setRawURI("/Mock/leen");
		originUri.setContextPath("/Mock");
		int rs=welcomeManager.welcome0(originUri, outUri);
		assertEquals(WelcomeManager.NOT_END_WITH_SLASH,rs);
	}
	
	public void testNotFound(){
		StringBuilder outUri=new StringBuilder();
		RequestURI originUri=new RequestURI();
		originUri.setRawURI("/Mock/lyq/");
		originUri.setContextPath("/Mock");
		int rs=welcomeManager.welcome0(originUri, outUri);
		System.out.println(outUri.toString());
		assertEquals(WelcomeManager.NOT_FOUND,rs);
	}
	
	public void testSuccess(){
		StringBuilder outUri=new StringBuilder();
		RequestURI originUri=new RequestURI();
		originUri.setRawURI("/Mock/leen/");
		originUri.setContextPath("/Mock");
		int rs=welcomeManager.welcome0(originUri, outUri);
		assertEquals(WelcomeManager.NORMAL,rs);
		assertEquals("/leen/test1",outUri.toString());
	}
	
	public void testRoot(){
		StringBuilder outUri=new StringBuilder();
		RequestURI originUri=new RequestURI();
		originUri.setRawURI("/Mock/");
		originUri.setContextPath("/Mock");
		int rs=welcomeManager.welcome0(originUri, outUri);
		assertEquals(WelcomeManager.NORMAL,rs);
		assertEquals("/index.html",outUri.toString());
	}
}
