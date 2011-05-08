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
		WAIT, USER, IRQ, SOFTIRQ, SYSCALL, TRAP, ZOMBIE, EXIT
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
	private final IdMap<FileDescriptor> fdsMap;
	private StateInfo lastWakeup;

	public Task(int pid, long createTs) {
		this();
		this.processId = pid;
		this.setStartTime(createTs);
	}

	public Task(int pid) {
		this(pid, 0);
	}
	
	public Task() {
		stateStack = new Stack<StateInfo>();
		listeners = new HashSet<ITaskListener>();
		isKernelThread = false;
		fdsMap = new IdMap<FileDescriptor>();
		fdsMap.setProvider(new IdProvider<FileDescriptor>() {
			@Override
			public int getId(FileDescriptor obj) {
				return obj.getFd();
			}
		});
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
		if (parent != null) {
			parent.pushState(this, nextState);
		} else {
			for (ITaskListener l: listeners) {
				l.pushState(this, nextState);
			}
		}
	}

	private void firePopState() {
		StateInfo nextState = null;
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

	public void setKernelThread(boolean isKernelThread) {
		this.isKernelThread = isKernelThread;
	}

	public boolean isKernelThread() {
		return isKernelThread;
	}

	public void addFileDescriptor(FileDescriptor fd) {
		FileDescriptor latest = fdsMap.getLatest(fd.getFd());
		if (latest != null && latest.isOpen()) {
			latest.setEndTime(fd.getStartTime());
		}
		fdsMap.add(fd);
		fd.setOwner(this);
	}

	public FileDescriptor getLatestFileDescriptor(int id) {
		return fdsMap.getLatest(id);
	}

	public HashMap<Integer, TreeSet<FileDescriptor>> getFileDescriptors() {
		return fdsMap.getMap();
	}

	public List<FileDescriptor> getOpenedFileDescriptors() {
		HashMap<Integer, TreeSet<FileDescriptor>> map = fdsMap.getMap();
		ArrayList<FileDescriptor> openedFd = new ArrayList<FileDescriptor>();
		TreeSet<FileDescriptor> set;
		for (Integer i: map.keySet()) {
			set = map.get(i);
			FileDescriptor last = set.last();
			if (last.isOpen()) {
				openedFd.add(last);
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

	public boolean matchSocket(SocketInet sock) {
		HashMap<Integer, TreeSet<FileDescriptor>> map = fdsMap.getMap();
		for (TreeSet<FileDescriptor> set: map.values()) {
			for (FileDescriptor fd: set) {
				if (fd instanceof SocketInet) {
					SocketInet s = (SocketInet) fd;
					if (s.getOwner() == this && s.isComplementary(sock)) {
						return true;
					}					
				}
			}
		}
		return false;
	}

}
