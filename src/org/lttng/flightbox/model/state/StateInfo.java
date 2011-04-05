package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;


public abstract class StateInfo {

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
		return taskState.toString();
	}
}
