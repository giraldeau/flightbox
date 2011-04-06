package org.lttng.flightbox.dep;

import java.util.TreeSet;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;

public class BlockingTree implements Comparable<BlockingTree> {

	private Task task;
	private StateInfo wakeUp;
	private SyscallInfo waitingSyscall;
	private long startTime;
	private long endTime;
	private TreeSet<BlockingTree> children;

	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public StateInfo getWakeUp() {
		return wakeUp;
	}
	public void setWakeUp(StateInfo wakeUp) {
		this.wakeUp = wakeUp;
	}
	public SyscallInfo getWaitingSyscall() {
		return waitingSyscall;
	}
	public void setWaitingSyscall(SyscallInfo waitingSyscall) {
		this.waitingSyscall = waitingSyscall;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public TreeSet<BlockingTree> getChildren() {
		return children;
	}
	public void setChildren(TreeSet<BlockingTree> children) {
		this.children = children;
	}
	@Override
	public int compareTo(BlockingTree o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.startTime < o.startTime) return BEFORE;
		if (this.startTime > o.startTime) return AFTER;
		return EQUAL;
	}

}
