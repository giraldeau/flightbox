package org.lttng.flightbox.model.state;

import java.util.EnumMap;
import java.util.Map;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;


public abstract class StateInfo {

	public enum Field {
		PREV_PID, NEXT_PID, PARENT_PID, CHILD_PID,
		PID, TGID, CHILD_TGID, FILENAME, IP, FD,
		STATE, CPU_ID, SRC_ADDR, SRC_PORT, DST_ADDR, DST_PORT,
		SOCKET, IS_CLIENT
	}

	private Task task;
	private TaskState taskState;
	private long start;
	private long end;
	private Map<Field, Object> fieldInfo;

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

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
	public long getDuration() {
		return end - start;
	}

	public void setField(Field key, Object value) {
		if (fieldInfo == null)
			fieldInfo = new EnumMap<Field, Object>(Field.class);
		fieldInfo.put(key, value);
	}

	public Object getField(Field key) {
		if (fieldInfo == null || !fieldInfo.containsKey(key))
			return null;
		return fieldInfo.get(key);
	}
}
