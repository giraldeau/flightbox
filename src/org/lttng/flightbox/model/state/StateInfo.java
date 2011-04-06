package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;


public abstract class StateInfo {

	private Task task;
	private TaskState taskState;
	private long start;
	private long end;

	public StateInfo() {
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setStartTime(long start) {
		this.start = start;
	}

	public long getStartTime() {
		return start;
	}

	public void setEndTime(long end) {
		this.end = end;
	}

	public long getEndTime() {
		return end;
	}

	@Override
	public String toString() {
		return "[state=" + taskState.toString() + ",pid=" + getTask().getProcessId() + "]";
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
}
