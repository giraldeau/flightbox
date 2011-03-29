package org.lttng.flightbox.model;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Model of a kernel task 
 *
 * @author Francis Giraldeau
 */
public class KernelTask implements Comparable<KernelTask> {

	public enum TaskState {
		USER, SYSCALL, TRAP, INTERRUPTED, PREEMPTED
	}
	
	private long createTime;
	private long exitTime;
	private int processId;
	private int threadGroupId;
	private KernelTask parentProcess;
	private List<KernelTask> childrenProcess;
	private int exitStatus;
	private String cmd;
	private Stack<TaskState> stateStack;
	
	public KernelTask(int pid, long createTs) {
		this();
		this.processId = pid;
		this.createTime = createTs;
	}
	
	public KernelTask() {
		stateStack = new Stack<TaskState>();
	}
	
	public boolean equals(Object other) {
		if (other instanceof KernelTask) {
			KernelTask p = (KernelTask) other;
			if (p.processId == this.processId && p.createTime == this.createTime) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return this.processId + (int)this.createTime;
	}
	
	public int compareTo(KernelTask o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.processId < o.processId) return BEFORE;
		if (this.processId > o.processId) return AFTER;
		if (this.createTime < o.createTime) return BEFORE;
		if (this.createTime > o.createTime) return AFTER;
		return EQUAL;
	}
	
	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getExitTime() {
		return exitTime;
	}

	public void setExitTime(long exitTime) {
		this.exitTime = exitTime;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public int getThreadGroupId() {
		return threadGroupId;
	}

	public void setThreadGroupId(int threadGroupId) {
		this.threadGroupId = threadGroupId;
	}

	public KernelTask getParentProcess() {
		return parentProcess;
	}

	public void setParentProcess(KernelTask parentProcess) {
		this.parentProcess = parentProcess;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public int getExitStatus() {
		return exitStatus;
	}
	
	public void addChild(KernelTask child) {
		if (childrenProcess == null) {
			childrenProcess = new Vector<KernelTask>();
		}
		childrenProcess.add(child);
	}
	
	public boolean removeChild(KernelTask child) {
		return childrenProcess.remove(child);
	}
	
	public boolean hasChild(KernelTask child) {
		return childrenProcess.contains(child);
	}
	
	public void pushState(TaskState state) {
		stateStack.push(state);
	}
	
	public void popState() {
		stateStack.pop();
	}
	
	public TaskState peekState() {
		return stateStack.peek();
	}
}
