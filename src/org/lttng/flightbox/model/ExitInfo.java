package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class ExitInfo extends StateInfo {

	public ExitInfo() {
		setTaskState(TaskState.EXIT);
	}

}
