package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class IRQInfo extends StateInfo {

	private int irqId;

	public IRQInfo() {
		setTaskState(TaskState.IRQ);
	}

	public void setIRQId(int irqId) {
		this.irqId = irqId;
	}

	public int getIRQId() {
		return irqId;
	}

}
