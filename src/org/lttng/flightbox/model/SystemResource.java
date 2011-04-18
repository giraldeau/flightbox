package org.lttng.flightbox.model;

public abstract class SystemResource implements ISystemResource {

	SystemModel parent;
	private long startTime;
	private long endTime;

	@Override
	public SystemModel getParent() {
		return parent;
	}

	@Override
	public void setParent(SystemModel model) {
		this.parent = model;
	}

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
