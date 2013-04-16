package leen.sc.request;

public class ServerInfo {
	private int port;
	private String serverName;
	private String scheme;

	public int getPort() {
		return port;
	}

	public String getServerName() {
		return serverName;
	}

	public String getScheme() {
		return scheme;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
}
