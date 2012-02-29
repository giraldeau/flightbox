package org.lttng.flightbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import org.lttng.flightbox.model.state.StateInfo;

/**
 * Model of a kernel task
 *
 * @author Francis Giraldeau
 */
public class Task extends SystemResource implements Comparable<Task> {

	public enum TaskState {
		ALIVE, 		/* after fork, before first scheduling */
		WAIT, 		/* not running, either waiting for CPU or blocked */ 
		USER, 		/* running in userspace */ 
		IRQ, 		/* hardware interrupt */ 
		SOFTIRQ,	/* software interrupt */
		SYSCALL, 	/* running in a system call */ 
		TRAP, 		/* page fault */
		ZOMBIE, 	/* exited, parent didn't read the exit status yet */
		EXIT 		/* process has finished, not yet unscheduled */
	}

	private int processId;
	private int threadGroupId;
	private Task parentProcess;
	private TreeSet<Task> childrenTask;
	private int exitStatus;
	private String cmd;
	private boolean isKernelThread;
	private final Stack<StateInfo> stateStack;
	private final HashSet<ITaskListener> listeners;
	private final FileDescriptorSet fdsSet;
	private StateInfo lastWakeup;
	private boolean listenersEnabled;

	public Task(int pid, long createTs) {
		this.processId = pid;
		setStartTime(createTs);
		setEnableListeners(true);
		stateStack = new Stack<StateInfo>();
		listeners = new HashSet<ITaskListener>();
		isKernelThread = false;
		fdsSet = new FileDescriptorSet();
	}

	public Task(int pid) {
		this(pid, 0);
	}
	
	public Task() {
		this(0, 0L);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Task) {
			Task p = (Task) other;
			if (p.processId == this.processId && p.getStartTime() == this.getStartTime()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.processId + (int)this.getStartTime();
	}

	@Override
	public int compareTo(Task o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.processId < o.processId) return BEFORE;
		if (this.processId > o.processId) return AFTER;
		if (this.getStartTime() < o.getStartTime()) return BEFORE;
		if (this.getStartTime() > o.getStartTime()) return AFTER;
		return EQUAL;
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

	public void setParentTask(Task parentProcess) {
		this.parentProcess = parentProcess;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void addChild(Task child) {
		if (childrenTask == null) {
			childrenTask = new TreeSet<Task>();
		}
		childrenTask.add(child);
	}

	public boolean removeChild(Task child) {
		return childrenTask.remove(child);
	}

	public boolean hasChild(Task child) {
		return childrenTask.contains(child);
	}

	public TreeSet<Task> getChildren() {
		return childrenTask;
	}

	public void pushState(StateInfo info) {
		info.setTask(this);
		firePushState(info);
		stateStack.push(info);
	}

	public StateInfo popState() {
		if (stateStack.isEmpty())
			return null;
		firePopState();
		return stateStack.pop();
	}

	public StateInfo peekState() {
		if (stateStack.isEmpty())
			return null;
		return stateStack.peek();
	}

	public StateInfo peekState(int i) {
		if (i > 0)
			return null;
		if (stateStack.size() <= Math.abs(i))
			return null;
		return stateStack.get((stateStack.size()-1)+i);
	}

	public void firePushState(StateInfo nextState) {
		if (!listenersEnabled)
			return;
		for (ITaskListener l: listeners) {
			l.pushState(this, nextState);
		}
	}

	private void firePopState() {
		if (!listenersEnabled)
			return;
		StateInfo nextState = null;
		if (stateStack.size() > 1) {
			nextState = stateStack.get(stateStack.size() - 2);
		}
		for (ITaskListener l: listeners) {
			l.popState(this, nextState);
		}
	}

	public void addListener(ITaskListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ITaskListener listener) {
		listeners.remove(listener);
	}

	public void setKernelThread(boolean isKernelThread) {
		this.isKernelThread = isKernelThread;
	}

	public boolean isKernelThread() {
		return isKernelThread;
	}

	public void addFileDescriptor(FileDescriptor fd) {
		FileDescriptor latest = fdsSet.getLatest(fd.getFd());
		if (latest != null && latest.isOpen()) {
			latest.setEndTime(fd.getStartTime());
		}
		fdsSet.add(fd);
		fd.setOwner(this);
	}

	public FileDescriptor getLatestFileDescriptor(int id) {
		return fdsSet.getLatest(id);
	}

	public HashMap<Integer, FileDescriptor> getFileDescriptors() {
		return fdsSet.getCurrent();
	}

	public FileDescriptorSet getFileDescriptorSet() {
		return fdsSet;
	}
	
	public List<FileDescriptor> getOpenedFileDescriptors() {
		HashMap<Integer, FileDescriptor> map = fdsSet.getCurrent();
		ArrayList<FileDescriptor> openedFd = new ArrayList<FileDescriptor>();
		for (FileDescriptor fd: map.values()) {
			if (fd.isOpen()) {
				openedFd.add(fd);
			}
		}
		return openedFd;
	}

	public void addFileDescriptors(List<FileDescriptor> fileDescriptors) {
		for (FileDescriptor f: fileDescriptors) {
			addFileDescriptor(f);
		}
	}

	public StateInfo getLastWakeUp() {
		return lastWakeup;
	}

	public void setLastWakeUp(StateInfo info) {
		this.lastWakeup = info;
	}
	
	@Override
	public String toString() {
		return "[task pid=" + processId + "]";
	}

	public SocketInet getSocketByIp(IPv4Con ip) {
		return fdsSet.findSocketByIp(ip);
	}

	public void setEnableListeners(boolean notify) {
		this.listenersEnabled = notify;
	}

	public boolean isListenersEnabled() {
		return this.listenersEnabled;
	}

	public List<StateInfo> getStates() {
		return this.stateStack;
	}
}
