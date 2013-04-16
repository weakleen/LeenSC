package leen.sc.container;

import java.io.IOException;

public class ClientAbortException extends IOException {
	public ClientAbortException(){}
	public ClientAbortException(String msg){super(msg);}
}
