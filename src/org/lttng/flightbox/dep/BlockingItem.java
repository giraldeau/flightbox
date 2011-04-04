package org.lttng.flightbox.dep;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.WaitInfo;

public class BlockingItem {

	private Task task;
	private WaitInfo waitInfo;

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void setWaitInfo(WaitInfo waitInfo) {
		this.waitInfo = waitInfo;
	}

	public WaitInfo getWaitInfo() {
		return waitInfo;
	}

}
