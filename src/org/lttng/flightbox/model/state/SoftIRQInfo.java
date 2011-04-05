package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;

public class SoftIRQInfo extends StateInfo {

	private int softirqId;

	public SoftIRQInfo() {
		setTaskState(TaskState.SOFTIRQ);
	}

	public void setSoftirqId(int softirqId) {
		this.softirqId = softirqId;
	}

	public int getSoftirqId() {
		return softirqId;
	}

}
