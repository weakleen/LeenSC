package leen.sc.connector;

import java.nio.channels.SocketChannel;

public interface NioProcessor {
	public void process(SocketChannel client);
}
