package leen.sc;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import leen.sc.connector.Connector;
import leen.sc.dispatcher.TimeCycleProcessor;
import leen.sc.response.BufferManager;
import leen.sc.response.BufferPool;
import leen.sc.response.PlainBufferManager;
import leen.sc.startup.util.CommonInfo;

import org.apache.log4j.Logger;

public class Leen {
	private static Logger log = Logger.getLogger(Leen.class);
	private static final String CONFIG_PATH = "conf/config.properties";

	public void start(ClassLoader rootClassLoader) {
		try {
			log.info("LeenSC Base " + CommonInfo.LEEN_BASE);

			Properties props=getProperties();
			
			// ��ʼ���̳߳�
			int maxThreads = Integer.valueOf(props.getProperty("maxThreads", "200"));
			int queueLength = Integer.valueOf(props.getProperty("queueLength", "200"));
			Executor executor = new ThreadPoolExecutor(maxThreads, maxThreads,
					0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
							queueLength));

			// ��ʼ�������
			int poolsize = maxThreads;
			int buffersize = Integer.valueOf(props.getProperty("buffersize", "1048567"));;
			BufferManager bufferMgr =null;
			if(props.getProperty("BufferManager","pool").equals("plain")){
				log.info("using palin buffer manager");
				bufferMgr=new PlainBufferManager(buffersize);	
			}
			else{
				log.info("using pooled buffer manager");
				bufferMgr=new BufferPool(poolsize, buffersize);
			}
			Processor processor = new Processor();
			processor.setExecutor(executor);
			processor.setBufferManager(bufferMgr);

			Connector con = new Connector();
			// configure connector
			int port=Integer.valueOf(props.getProperty("port", "80"));
			log.info("using port "+port);
			con.setPort(port);
			con.setProcessor(processor);
			con.setExecutor(executor);
			con.init();
			processor.start();
			con.start();

			// ���������¼�������
			TimeCycleProcessor.getInstance().startService();
		} catch (ConfigException e) {
			log.error("���ô���"+e.getMessage());
		}
		catch (NumberFormatException e) {
			log.error("�˿����ô���");
		}
	}

	public Properties getProperties() throws ConfigException {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(CONFIG_PATH));
			return p;
		} catch (Exception e) {
			throw new ConfigException("config.properties not found");
		}
	}
}
