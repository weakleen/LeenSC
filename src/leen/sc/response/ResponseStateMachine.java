package leen.sc.response;

public class ResponseStateMachine {
	enum ResponseState{
		STARTED,COMMITED,CLOSED,FINISHED
	}
	
	private ResponseState state=ResponseState.STARTED;
	
	public ResponseState getState(){
		return state;
	}

	public boolean isClosed(){
		if(state==ResponseState.CLOSED||state==ResponseState.FINISHED)
			return true;
		return false;
	}
	
	public boolean isCommited(){
		if(state!=ResponseState.STARTED)
			return true;
		return false;
	}

	public boolean isFinished(){
		if(state==ResponseState.FINISHED)
			return true;
		return false;
	}
	
	public synchronized void commit(){
		if(state!=ResponseState.STARTED)
			throw new IllegalStateException("ERROR STATE");
		state=ResponseState.COMMITED;
	}
	
	public synchronized void close(){
		if(state!=ResponseState.COMMITED)
			throw new IllegalStateException("ERROR STATE");
		state=ResponseState.CLOSED;
	}
	
	public synchronized void finish(){
		if(state!=ResponseState.CLOSED)
			throw new IllegalStateException("ERROR STATE");
		state=ResponseState.FINISHED;
	}
}
