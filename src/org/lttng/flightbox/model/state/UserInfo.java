package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;

public class UserInfo extends StateInfo {

	public UserInfo() {
		setTaskState(TaskState.USER);
	}

}
