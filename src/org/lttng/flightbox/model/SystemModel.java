package org.lttng.flightbox.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.model.Processor.ProcessorState;
import org.lttng.flightbox.model.state.StateInfo;

public class SystemModel extends AbstractTaskListener implements IProcessorListener {

	/**
	 * Current process list
	 */
	private final HashMap<Integer, TreeSet<Task>> tasksByPid;
	
	/**
	 * Latest process list (without history)
	 */
	private final HashMap<Integer, Task> latestTaskMap;

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
	 * Blocking model
	 */
	private final BlockingModel blockingModel;
	
	/**
	 * System calls table
	 */

	private final SymbolTable syscallTable;

	private final SymbolTable interruptTable;

	private final SymbolTable softirqTable;

	/**
	 * File descriptors
	 * an fd can be reused and is unique per process
	 * we want to keep all fd history
	 */

	public SystemModel() {
		processors = new ArrayList<Processor>();
		processorListeners = new ArrayList<IProcessorListener>();
		taskListeners = new ArrayList<ITaskListener>();
		syscallTable = new SymbolTable();
		interruptTable = new SymbolTable();
		softirqTable = new SymbolTable();
		tasksByPid = new HashMap<Integer, TreeSet<Task>>();
		blockingModel = new BlockingModel();
		latestTaskMap = new HashMap<Integer, Task>();
	}

	public void initProcessors(int numOfProcessors) {
		processors.clear();
		for (int i=0; i < numOfProcessors; i++) {
			Processor p = new Processor(i);
			processors.add(p);
			p.addListener(this);
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

	public void addTask(Task task) {
		int pid = task.getProcessId();
		TreeSet<Task> taskSet = tasksByPid.get(pid);
		if (taskSet == null) {
			taskSet = new TreeSet<Task>();
			tasksByPid.put(pid, taskSet);
		}
		taskSet.add(task);
		latestTaskMap.put(task.getProcessId(), task);
		task.addListener(this);
	}

	public void removeTask(Task task) {
		TreeSet<Task> taskSet = tasksByPid.get(task.getProcessId());
		if (taskSet != null && taskSet.contains(task)) {
			taskSet.remove(task);
		}
		latestTaskMap.remove(task.getProcessId());
		task.removeListener(this);
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
		listener.setModel(this);
	}

	public void removeTaskListener(ITaskListener listener) {
		taskListeners.remove(listener);
	}

	public SymbolTable getSyscallTable() {
		return syscallTable;
	}

	public SymbolTable getInterruptTable() {
		return interruptTable;
	}

	public SymbolTable getSoftIRQTable() {
		return softirqTable;
	}

	public HashMap<Integer, TreeSet<Task>> getTasks() {
		return tasksByPid;
	}

	public HashMap<Integer, Task> getLatestTasks() {
		return latestTaskMap;
	}
	
	public Task getLatestTaskByPID(int pid) {
		Task task = null;
		TreeSet<Task> taskSet = tasksByPid.get(pid);
		if (taskSet != null)
			task = taskSet.last();
		return task;
	}

	public TreeSet<Task> getTaskByCmd(String cmd, boolean basename) {
		TreeSet<Task> resultSet = new TreeSet<Task>();
		for (TreeSet<Task> set: tasksByPid.values()) {
			for (Task t: set) {
				if (t.getCmd() == null)
					continue;
				if (basename) {
					if (t.getCmd().endsWith(cmd)) {
						resultSet.add(t);
					}
				} else {
					if (t.getCmd().equals(cmd)) {
						resultSet.add(t);
					}
				}
			}
		}
		return resultSet;
	}

	public TreeSet<Task> getTaskByCmdBasename(String cmd) {
		return getTaskByCmd(cmd, true);
	}
	
	public Task getLatestTaskByCmd(String cmd) {
		TreeSet<Task> taskByCmd = getTaskByCmd(cmd, false);
		if (taskByCmd.isEmpty())
			return null;
		return taskByCmd.last();
	}

	public Task getLatestTaskByCmdBasename(String cmd) {
		TreeSet<Task> taskByCmd = getTaskByCmd(cmd, true);
		if (taskByCmd.isEmpty())
			return null;
		return taskByCmd.last();
	}

	public BlockingModel getBlockingModel() {
		return blockingModel;
	}

	/* search into latest tasks only */
	public SocketInet findComplementSocket(SocketInet sock) {
		if (!sock.getIp().isSet())
			return null;
		IPv4Con complementCon = sock.getIp().getComplement();
		HashMap<Integer, Task> tasks = getLatestTasks();
		for (Task task: tasks.values()) {
			SocketInet found = task.getSocketByIp(complementCon);
			if (found != null)
				return found;
		}
		return null;
	}
}
