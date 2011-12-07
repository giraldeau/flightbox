package org.lttng.flightbox.model;

import java.util.Collection;
import java.util.HashSet;

import javax.jws.WebParam.Mode;

import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;

public class LoggingTaskListener extends AbstractTaskListener {

	HashSet<Integer> pids;
	
	public LoggingTaskListener() {
		this.pids = new HashSet<Integer>();
	}
	
	public void addAllPid(Collection<Integer> pids) {
		this.pids.addAll(pids);
	}

	public void addPid(Integer pids) {
		this.pids.add(pids);
	}
	
	@Override
	public void pushState(Task task, StateInfo nextState) {
		log("push", task, nextState);
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
		log("pop", task, nextState);
	}
	
	public void log(String op, Task task, StateInfo nextState) {
		if (pids.contains(task.getProcessId()) || pids.isEmpty()) {
			String str = "";
			if (nextState != null) {
				if (nextState.getTaskState() == TaskState.SYSCALL) {
					SyscallInfo info = (SyscallInfo) nextState;
					str = model.getSyscallTable().get(info.getSyscallId());
				} else {
					str = nextState.toString();
				}
			}
			System.out.println(String.format("%-5s %-6d %s", op, task.getProcessId(), str));
		}
	}
}
