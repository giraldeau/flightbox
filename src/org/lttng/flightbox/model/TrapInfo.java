package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class TrapInfo extends StateInfo {

	public TrapInfo() {
		setTaskState(TaskState.TRAP);
	}

}
