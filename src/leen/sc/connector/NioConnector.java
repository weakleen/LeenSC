package leen.sc.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NioConnector {
	private String host;
	private int port;
	private Executor executor=Executors.newFixedThreadPool(200);
	private NioProcessor processor;
	
	public NioConnector() {
	}

	public void start(){
		try {
			Selector selector=Selector.open();
			ServerSocketChannel server=ServerSocketChannel.open();
			InetSocketAddress addr = new InetSocketAddress(host, port);
			server.socket().bind(addr);
			server.configureBlocking(false);
			SocketChannel client = null;
			server.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						server = (ServerSocketChannel) key.channel();
						client = server.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
					}
					if (key.isReadable()) {
						process(client);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void process(SocketChannel client){
		executor.execute(new Work(client));
	}
	
	class Work implements Runnable{
		
		private SocketChannel client;
		
		public Work(SocketChannel client) {
			this.client=client;
		}
		
		@Override
		public void run() {
			processor.process(client);
			ByteBuffer.allocate(100);
		}
		
	}
}


