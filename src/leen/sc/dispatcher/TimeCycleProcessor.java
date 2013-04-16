package leen.sc.dispatcher;

import java.util.HashSet;
import java.util.Set;

public class TimeCycleProcessor implements Runnable {
	private Set<TimeCycle> timeCycles = new HashSet<TimeCycle>();
	private boolean stopped = false;
	private boolean started = false;

	private static TimeCycleProcessor instance;

	public static TimeCycleProcessor getInstance() {
		if (instance == null) {
			synchronized (TimeCycleProcessor.class) {
				if (instance == null)
					instance = new TimeCycleProcessor();
			}
		}
		return instance;
	}

	private TimeCycleProcessor() {
	}

	public synchronized void addTimeCycle(TimeCycle timeCycle)  {
		if (timeCycle == null)
			throw new IllegalArgumentException();
		timeCycles.add(timeCycle);
	}
	
	public synchronized void removeTimeCycle(TimeCycle timeCycle)  {
		if (timeCycle == null)
			throw new IllegalArgumentException();
		timeCycles.remove(timeCycle);
	}

	public boolean existTimeCycle(TimeCycle timeCycle){
		if (timeCycle == null)
			throw new IllegalArgumentException();
		return timeCycles.contains(timeCycle);
	}
	
	@Override
	public void run() {
		while (!stopped) {
			Set<TimeCycle> toRemove = new HashSet<TimeCycle>();
			for (TimeCycle timeCycle : timeCycles) {
				if (timeCycle.timeEvent())
					toRemove.add(timeCycle);
			}
			timeCycles.removeAll(toRemove);
			try {
				Thread.sleep(TimeCycle.TIME_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void startService() {
		if (started)
			throw new IllegalStateException();
		started=true;
		new Thread(this).start();

	}
	
	public boolean isStarted(){
		return started;
	}

	public void stopService() {
		if (!stopped)
			stopped = true;
	}
}
