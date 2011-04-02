package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class TaskListener implements ITaskListener {

	@Override
	public void pushState(Task task, TaskState nextState) {
	}

	@Override
	public void popState(Task task, TaskState nextState) {
	}

}
