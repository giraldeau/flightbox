package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;

public class AliveInfo extends StateInfo {

	public AliveInfo() {
		setTaskState(TaskState.ALIVE);
	}

}
