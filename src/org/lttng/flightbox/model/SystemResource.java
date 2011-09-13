package org.lttng.flightbox.model;

public abstract class SystemResource implements ISystemResource {

	private long startTime;
	private long endTime;

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getEndTime() {
		return endTime;
	}
}
