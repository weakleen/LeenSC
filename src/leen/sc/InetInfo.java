package leen.sc;

import java.net.InetAddress;

public class InetInfo {
	private InetAddress localAddr;
	private InetAddress remoteAddr;
	private int localPort;
	private int remotePort;

	public InetInfo(InetAddress localAddr, InetAddress remoteAddr, int localPort,int remotePort) {
		if (localAddr == null || remoteAddr == null || localPort < 0||remotePort<0)
			throw new IllegalArgumentException();
		this.localAddr = localAddr;
		this.remoteAddr = remoteAddr;
		this.localPort = localPort;
		this.remotePort = remotePort;
	}

	public InetAddress getLocalAddr() {
		return localAddr;
	}

	public InetAddress getRemoteAddr() {
		return remoteAddr;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getRemotePort() {
		return remotePort;
	}
	
	
}
