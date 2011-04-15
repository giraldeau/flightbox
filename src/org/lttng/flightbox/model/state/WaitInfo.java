package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;

public class WaitInfo extends StateInfo {

	private StateInfo wakeUp;
	private boolean isBlocking;
	private SyscallInfo waitingSyscall;
	private Task wakeUpProcess;

	public WaitInfo() {
		setTaskState(TaskState.WAIT);
		setBlocking(false);
	}

	public void setWakeUp(StateInfo wakeUp) {
		this.wakeUp = wakeUp;
	}

	public StateInfo getWakeUp() {
		return wakeUp;
	}

	public void setBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}

	public boolean isBlocking() {
		return isBlocking;
	}

	public SyscallInfo getWaitingSyscall() {
		return this.waitingSyscall;
	}

	public void setWaitingSyscall(SyscallInfo syscallInfo) {
		this.waitingSyscall = syscallInfo;
	}

	public void setWakeUpProcess(Task task) {
		this.wakeUpProcess = task;
	}

	public Task getWakeUpTask() {
		return this.wakeUpProcess;
	}
}
