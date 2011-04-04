package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class WaitInfo extends StateInfo {

	public enum WaitType {
		IO, CPU, TIMER, LOCK
	}

	private WaitType wait;
	private StateInfo wakeUp;
	private boolean isBlocking;
	private SyscallInfo waitingSyscall;

	public WaitInfo() {
		setTaskState(TaskState.WAIT);
		setWait(WaitType.CPU);
		setBlocking(false);
	}

	public void setWait(WaitType wait) {
		this.wait = wait;
	}

	public WaitType getWait() {
		return wait;
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

}
