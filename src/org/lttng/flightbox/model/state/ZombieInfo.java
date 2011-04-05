package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;

public class ZombieInfo extends StateInfo {

	public ZombieInfo() {
		setTaskState(TaskState.ZOMBIE);
	}

}
