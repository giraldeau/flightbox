package org.lttng.flightbox.model;

public class SoftIRQInfo extends StateInfo {

	private int softirqId;

	public void setSoftirqId(int softirqId) {
		this.softirqId = softirqId;
	}

	public int getSoftirqId() {
		return softirqId;
	}

	@Override
	public void reset() {
		softirqId = 0;
	}

}
