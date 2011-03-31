package org.lttng.flightbox.io;

public class TimeKeeper {

	private long currentTime = 0;

	private static TimeKeeper instance = null;
	
	private TimeKeeper() {
	}
	
	public static TimeKeeper getInstance() {
		if (instance == null) {
			instance = new TimeKeeper();
		}
		return instance;
	}
	
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}

	public long getCurrentTime() {
		return currentTime;
	}
	
}
