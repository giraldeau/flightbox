package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public interface ITaskListener {

	public void pushState(Task task, TaskState nextState);

	public void popState(Task task, TaskState nextState);

}
