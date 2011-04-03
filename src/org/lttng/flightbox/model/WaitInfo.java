package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class WaitInfo extends StateInfo {

	public enum WaitType {
		IO, CPU, TIMER, LOCK
	}

	private WaitType wait;
	private StateInfo wakeUp;
	private StateInfo waitParent;
	private boolean isBlocking;

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

	public void setWaitParent(StateInfo waitParent) {
		this.waitParent = waitParent;
	}

	public StateInfo getWaitParent() {
		return waitParent;
	}

}
