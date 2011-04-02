package org.lttng.flightbox.model;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

/**
 * Model of a kernel task
 *
 * @author Francis Giraldeau
 */
public class Task extends SystemResource implements Comparable<Task> {

	public enum TaskState {
		READY, USER, IRQ, SOFTIRQ, SYSCALL, TRAP, ZOMBIE, EXIT
	}

	private long createTime;
	private long exitTime;
	private int processId;
	private int threadGroupId;
	private Task parentProcess;
	private List<Task> childrenProcess;
	private int exitStatus;
	private String cmd;
	private final Stack<TaskState> stateStack;
	private final Map<TaskState, Stack<StateInfo>> stateInfo;
	private final Map<TaskState, Stack<StateInfo>> stateInfoCache;
	private final HashSet<ITaskListener> listeners;

	public Task(int pid, long createTs) {
		this();
		this.processId = pid;
		this.createTime = createTs;
	}

	public Task() {
		stateStack = new Stack<TaskState>();
		listeners = new HashSet<ITaskListener>();
		stateInfo = new EnumMap<TaskState, Stack<StateInfo>>(TaskState.class);
		stateInfoCache = new EnumMap<TaskState, Stack<StateInfo>>(TaskState.class);
		for (TaskState state: TaskState.values()) {
			stateInfo.put(state, new Stack<StateInfo>());
			stateInfoCache.put(state, new Stack<StateInfo>());
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Task) {
			Task p = (Task) other;
			if (p.processId == this.processId && p.createTime == this.createTime) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.processId + (int)this.createTime;
	}

	@Override
	public int compareTo(Task o) {
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

	public Task getParentProcess() {
		return parentProcess;
	}

	public void setParentProcess(Task parentProcess) {
		this.parentProcess = parentProcess;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void addChild(Task child) {
		if (childrenProcess == null) {
			childrenProcess = new Vector<Task>();
		}
		childrenProcess.add(child);
	}

	public boolean removeChild(Task child) {
		return childrenProcess.remove(child);
	}

	public boolean hasChild(Task child) {
		return childrenProcess.contains(child);
	}

	public StateInfo setupStateInfo(TaskState state) {
		Stack<StateInfo> stack = stateInfo.get(state);
		Stack<StateInfo> oldStack = stateInfoCache.get(state);
		StateInfo info = null;
		if (stack.size() == oldStack.size()) {
			info = StateInfoFactory.makeStateInfo(state);
		} else if (stack.size() < oldStack.size()) {
			info = oldStack.get(stack.size() - 1);
			if (info != null)
				info.reset();
		} else {
			throw new RuntimeException("size of state info cache should never be less than current stack size");
		}
		if (info != null)
			stack.push(info);
		return info;
	}

	public void pushState(TaskState state) {
		firePushState(state);
		stateStack.push(state);
	}

	public void popState() {
		if (stateStack.isEmpty())
			return;
		firePopState();
		TaskState state = stateStack.peek();
		Stack<StateInfo> stack = stateInfo.get(state);
		if (!stack.isEmpty())
			stack.pop();
		stateStack.pop();
	}

	public TaskState peekState() {
		if (stateStack.isEmpty())
			return null;
		return stateStack.peek();
	}

	public StateInfo peekStateInfo(TaskState state) {
		if (state == null)
			return null;
		Stack<StateInfo> stack = stateInfo.get(state);
		if (stack.isEmpty())
			return null;
		return stack.peek();
	}

	public void firePushState(TaskState nextState) {
		if (parent != null) {
			parent.pushState(this, nextState);
		} else {
			for (ITaskListener l: listeners) {
				l.pushState(this, nextState);
			}
		}
	}

	private void firePopState() {
		TaskState nextState = null;
		if (stateStack.size() > 1) {
			nextState = stateStack.get(stateStack.size() - 2);
		}
		if (parent != null) {
			parent.popState(this, nextState);
		} else {
			for (ITaskListener l: listeners) {
				l.popState(this, nextState);
			}
		}
	}

	public void addListener(ITaskListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ITaskListener listener) {
		listeners.remove(listener);
	}

	public StateInfo getCurrentStateInfo() {
		if (stateStack.isEmpty())
			return null;
		return stateInfo.get(stateStack.peek()).peek();
	}
}
