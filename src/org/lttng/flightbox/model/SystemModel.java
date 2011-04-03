package org.lttng.flightbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.lttng.flightbox.model.Processor.ProcessorState;

public class SystemModel implements IProcessorListener, ITaskListener {

	/**
	 * Current process list
	 */
	private final HashMap<Integer, HashSet<Task>> tasks;

	/**
	 * All processors
	 */
	private final ArrayList<Processor> processors;

	/**
	 * Processor listeners
	 */
	private final ArrayList<IProcessorListener> processorListeners;

	/**
	 * Task listeners
	 */
	private final ArrayList<ITaskListener> taskListeners;

	/**
	 * System calls table
	 */

	private final SyscallTable syscallTable;

	public SystemModel() {
		processors = new ArrayList<Processor>();
		processorListeners = new ArrayList<IProcessorListener>();
		taskListeners = new ArrayList<ITaskListener>();
		syscallTable = new SyscallTable();
		tasks = new HashMap<Integer, HashSet<Task>>();
	}

	public void initProcessors(int numOfProcessors) {
		processors.clear();
		for (int i=0; i < numOfProcessors; i++) {
			Processor p = new Processor(i);
			p.setParent(this);
			processors.add(p);
		}
	}

	public List<Processor> getProcessors() {
		return processors;
	}

	public void addProcessorListener(IProcessorListener listener) {
		processorListeners.add(listener);
	}

	public void removeProcessorListener(IProcessorListener listener) {
		processorListeners.remove(listener);
	}

	public void addTask(Task t1) {
		int pid = t1.getProcessId();
		HashSet<Task> taskSet = tasks.get(pid);
		if (taskSet == null) {
			taskSet = new HashSet<Task>();
			tasks.put(pid, taskSet);
		}
		taskSet.add(t1);
		t1.setParent(this);
	}

	public void removeTask(Task t1) {
		HashSet<Task> taskSet = tasks.get(t1.getProcessId());
		if (taskSet != null && taskSet.contains(t1)) {
			taskSet.remove(t1);
		}
		t1.setParent(null);
	}

	@Override
	public void stateChange(Processor processor, ProcessorState nextState) {
		for (IProcessorListener listener: processorListeners) {
			listener.stateChange(processor, nextState);
		}
	}

	@Override
	public void lowPowerModeChange(Processor processor, boolean nextLowPowerMode) {
		for (IProcessorListener listener: processorListeners) {
			listener.lowPowerModeChange(processor, nextLowPowerMode);
		}
	}

	@Override
	public void pushState(Task task, StateInfo nextState) {
		for (ITaskListener listener: taskListeners) {
			listener.pushState(task, nextState);
		}
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
		for (ITaskListener listener: taskListeners) {
			listener.popState(task, nextState);
		}
	}

	public void addTaskListener(ITaskListener listener) {
		taskListeners.add(listener);
	}

	public void removeTaskListener(ITaskListener listener) {
		taskListeners.remove(listener);
	}

	public SyscallTable getSyscallTable() {
		return syscallTable;
	}

}
