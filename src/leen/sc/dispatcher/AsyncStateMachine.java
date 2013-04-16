package leen.sc.dispatcher;

public class AsyncStateMachine {
	enum AsyncState {
		DISPATCHING, DISPATCHED, COMPLETED, ASYNCWAIT, ASYNCSTARTED, REDISPATCHING, COMPLETING;
	}

	private AsyncState state = AsyncState.ASYNCSTARTED;

	public AsyncState getState() {
		return state;
	}

	// 判断当前状态是否允许添加监听器
	public boolean allowAddListener() {
		if (state != AsyncState.DISPATCHING && state != AsyncState.ASYNCWAIT
				&& state != AsyncState.COMPLETED
				&& state != AsyncState.DISPATCHED)
			return true;
		return false;
	}

	public boolean isCompleted() {
		if (state == AsyncState.COMPLETED)
			return true;
		return false;
	}

	// 该方法用于判断AC是否启动，
	public boolean isStarted() {
		if (state == AsyncState.ASYNCSTARTED || state == AsyncState.COMPLETING
				|| state == AsyncState.REDISPATCHING||state==AsyncState.ASYNCWAIT)
			return true;
		return false;
	}

	public synchronized void preService() throws IllegalStateException {
		if (state == AsyncState.DISPATCHING)
			state = AsyncState.DISPATCHED;
		else
			throw new IllegalStateException("ERROR STATE");
	}

	public synchronized void postService() throws IllegalStateException {
		if (state == AsyncState.DISPATCHED)
			state = AsyncState.COMPLETED;
		else if (state == AsyncState.ASYNCSTARTED)
			state = AsyncState.ASYNCWAIT;
		else if (state == AsyncState.REDISPATCHING)
			state = AsyncState.DISPATCHING;
		else if (state == AsyncState.COMPLETING)
			state = AsyncState.COMPLETED;
		else
			throw new IllegalStateException("ERROR STATE "+state);
	}

	public synchronized void startAsync() throws IllegalStateException {
		if (state == AsyncState.DISPATCHED)
			state = AsyncState.ASYNCSTARTED;
		else
			throw new IllegalStateException("ERROR STATE " + state);
	}

	public synchronized void dispatch() {
		if (state == AsyncState.ASYNCWAIT)
			state = AsyncState.DISPATCHING;
		else if (state == AsyncState.ASYNCSTARTED)
			state = AsyncState.REDISPATCHING;
		else
			throw new IllegalStateException("ERROR STATE:"+state);
	}

	public synchronized void dispatched() {
		if (state == AsyncState.DISPATCHING)
			state = AsyncState.DISPATCHED;
		else
			throw new IllegalStateException("ERROR STATE " + state);
	}

	public synchronized void complete() throws IllegalStateException {
		if (state == AsyncState.ASYNCWAIT)
			state = AsyncState.COMPLETED;
		else if (state == AsyncState.ASYNCSTARTED)
			state = AsyncState.COMPLETING;
		else {
			throw new IllegalStateException("ERROR STATE " + state);
		}
	}

	public boolean isDispatching() {
		if (state == AsyncState.DISPATCHING)
			return true;
		return false;
	}
	

	public boolean isRedispatching() {
		if (state == AsyncState.REDISPATCHING)
			return true;
		return false;
	}
}
