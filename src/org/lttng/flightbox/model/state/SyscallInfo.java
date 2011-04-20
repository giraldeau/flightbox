package org.lttng.flightbox.model.state;

import org.lttng.flightbox.model.Task.TaskState;

public class SyscallInfo extends StateInfo {

	private int syscallId;
	private int retCode;

	public SyscallInfo() {
		setTaskState(TaskState.SYSCALL);
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setSyscallId(int syscallId) {
		this.syscallId = syscallId;
	}

	public int getSyscallId() {
		return syscallId;
	}

}
