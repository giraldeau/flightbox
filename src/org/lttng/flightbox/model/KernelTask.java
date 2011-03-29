package org.lttng.flightbox.model;


/**
 * @author Francis Giraldeau
 *
 * Model of a kernel task 
 *
 */
public class KernelTask implements Comparable<KernelTask> {

	private long createTime;
	private long exitTime;
	private int processId;
	private int threadGroupId;
	private int parentProcessId;
	private String cmd;
	
	public KernelTask(int pid, long createTs) {
		this.processId = pid;
		this.createTime = createTs;
	}
	
	public KernelTask() {
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

	public int getParentProcessId() {
		return parentProcessId;
	}

	public void setParentProcessId(int parentProcessId) {
		this.parentProcessId = parentProcessId;
	}
	
}
