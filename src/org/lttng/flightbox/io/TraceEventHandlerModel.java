package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.StateInfoFactory;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.IRQInfo;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;
import org.lttng.flightbox.model.state.SyscallInfo.Field;
import org.lttng.flightbox.model.state.WaitInfo;

public class TraceEventHandlerModel extends TraceEventHandlerBase {

	private SystemModel model;

	public TraceEventHandlerModel() {
		super();
		hooks.add(new TraceHook("kernel", "sched_schedule"));
		hooks.add(new TraceHook("kernel", "sched_try_wakeup"));
		hooks.add(new TraceHook("kernel", "process_fork"));
		hooks.add(new TraceHook("kernel", "process_exit"));
		hooks.add(new TraceHook("kernel", "syscall_entry"));
		hooks.add(new TraceHook("kernel", "syscall_exit"));
		hooks.add(new TraceHook("kernel", "irq_entry"));
		hooks.add(new TraceHook("kernel", "irq_exit"));
		hooks.add(new TraceHook("kernel", "softirq_entry"));
		hooks.add(new TraceHook("kernel", "softirq_exit"));
		hooks.add(new TraceHook("net", "socket_create"));
		hooks.add(new TraceHook("fs", "exec"));
		//hooks.add(new TraceHook("fs", "open"));
		//hooks.add(new TraceHook("fs", "close"));
	}

	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		if (model == null)
			throw new RuntimeException("Error: model must not be null");
		model.initProcessors(trace.getCpuNumber());
	}

	@Override
	public void handleComplete(TraceReader reader) {

	}

	public void handle_kernel_syscall_entry(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		Long syscallId = (Long) event.parseFieldByName("syscall_id");
		SyscallInfo info = (SyscallInfo) StateInfoFactory.makeStateInfo(TaskState.SYSCALL);
		info.setStartTime(eventTs);
		info.setSyscallId(syscallId.intValue());
		currentTask.pushState(info);
	}

	public void handle_kernel_syscall_exit(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		Long syscallRet = (Long) event.parseFieldByName("ret");
		StateInfo info = currentTask.peekState();

		if (info == null || info.getTaskState() != TaskState.SYSCALL)
			return;

		SyscallInfo state = (SyscallInfo) info;
		state.setEndTime(eventTs);
		state.setRetCode(syscallRet.intValue());

		switch (state.getSyscallId()) {
		case SymbolTable.SYS_OPEN:
			if (state.getRetCode() < 0)
				break;
			FileDescriptor fd = new FileDescriptor();
			fd.setFd((Integer)state.getField(Field.FD));
			fd.setStartTime(eventTs);
			model.addFileDescriptor(currentTask, fd);
			break;
		case SymbolTable.SYS_SOCKET:
			break;
		case SymbolTable.SYS_CONNECT:
			break;
		case SymbolTable.SYS_READ:
			break;
		case SymbolTable.SYS_WRITE:
			break;
		case SymbolTable.SYS_CLOSE:
			if (state.getRetCode() < 0)
				break;
			FileDescriptor f = new FileDescriptor();
			f.setFd((Integer)state.getField(Field.FD));
			f.setStartTime(eventTs);
			break;
		}

		currentTask.popState();

	}

	public void handle_kernel_irq_entry(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		Long irq = (Long) event.parseFieldByName("irq_id");

		IRQInfo info = (IRQInfo) StateInfoFactory.makeStateInfo(TaskState.IRQ);
		info.setStartTime(eventTs);
		info.setIRQId(irq.intValue());
		currentTask.pushState(info);
	}

	public void handle_kernel_irq_exit(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		StateInfo info = currentTask.peekState();
		if (info == null || info.getTaskState() != TaskState.IRQ)
			return;

		IRQInfo state = (IRQInfo) info;
		state.setEndTime(eventTs);
		currentTask.popState();
	}

	public void handle_kernel_softirq_entry(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		Long irq = (Long) event.parseFieldByName("softirq_id");

		SoftIRQInfo info = (SoftIRQInfo) StateInfoFactory.makeStateInfo(TaskState.SOFTIRQ);
		info.setStartTime(eventTs);
		info.setSoftirqId(irq.intValue());
		currentTask.pushState(info);
	}

	public void handle_kernel_softirq_exit(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long eventTs = event.getEventTime().getTime();
		StateInfo info = currentTask.peekState();
		if (info == null || info.getTaskState() != TaskState.SOFTIRQ)
			return;

		SoftIRQInfo state = (SoftIRQInfo) info;
		state.setEndTime(eventTs);
		currentTask.popState();
	}

	public void handle_kernel_sched_schedule(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		Long eventTs = event.getEventTime().getTime();
		Long prevPid = (Long) event.parseFieldByName("prev_pid");
		Long nextPid = (Long) event.parseFieldByName("next_pid");

		Task nextTask = model.getLatestTaskByPID(nextPid.intValue());
		Task prevTask = model.getLatestTaskByPID(prevPid.intValue());

		if (nextTask == null || prevTask == null)
			return;

		// update current task on given CPU
		Processor p = model.getProcessors().get(cpu.intValue());
		p.setCurrentTask(nextTask);

		// Pop waiting state on the scheduled in task
		StateInfo infoNext = nextTask.peekState();
		if (infoNext != null) {
			infoNext.setEndTime(eventTs);
			nextTask.popState();
		}

		StateInfo prevInfo = prevTask.peekState();

		// special case if the task exits
		if (prevInfo != null && prevInfo.getTaskState() == TaskState.EXIT) {
			// last schedule of the process
			ExitInfo exit = (ExitInfo) prevInfo;
			exit.setEndTime(eventTs);
			prevTask.setEndTime(eventTs);
			prevTask.popState();
			return;
		}

		// Push waiting state on the task that is scheduled out
		WaitInfo waitInfoPrev = (WaitInfo) StateInfoFactory.makeStateInfo(TaskState.WAIT);
		waitInfoPrev.setStartTime(eventTs);
		prevTask.pushState(waitInfoPrev);
	}

	public void handle_kernel_sched_try_wakeup(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		long eventTs = event.getEventTime().getTime();
		Long pid = (Long) event.parseFieldByName("pid");
		Task wakedTask = model.getLatestTaskByPID(pid.intValue());
		Task currentTask = model.getProcessors().get(cpu.intValue()).getCurrentTask();
		StateInfo info = wakedTask.peekState();

		if (info == null)
			return;

		info.setEndTime(eventTs);

		StateInfo wakeCause = null;
		if (currentTask != null)
			wakeCause = currentTask.peekState();

		StateInfo waitInState = wakedTask.peekState(-1);
		if (waitInState == null)
			return;

		/*
		 * try_wakeup can happen on already running task
		 * make sure the task was waiting
		 */
		if (info instanceof WaitInfo) {
			WaitInfo wait = (WaitInfo) info;
			wait.setBlocking(true);
			wait.setEndTime(eventTs);
			if (currentTask != null && currentTask.isKernelThread() && wakeCause == null) {
				wait.setWakeUpProcess(currentTask);
			} else {
				wait.setWakeUp(wakeCause);
			}
			if (waitInState.getTaskState() == TaskState.SYSCALL) {
				wait.setWaitingSyscall((SyscallInfo)waitInState);
			}
		}
	}

	public void handle_fs_exec(TraceReader reader, JniEvent event) {
			Long cpu = event.getParentTracefile().getCpuNumber();
			String filename = (String) event.parseFieldByName("filename");
			Processor p = model.getProcessors().get(cpu.intValue());
			Task currentTask = p.getCurrentTask();
			currentTask.setCmd(filename);
	}

	public void handle_kernel_process_fork(TraceReader reader, JniEvent event) {
		Long parentPid = (Long) event.parseFieldByName("parent_pid");
		Long childPid = (Long) event.parseFieldByName("child_pid");
		long eventTs = event.getEventTime().getTime();
		Task task = new Task();
		task.setProcessId(childPid.intValue());
		task.setStartTime(eventTs);
		Task parentTask = model.getLatestTaskByPID(parentPid.intValue());
		if (parentTask != null) {
			task.setCmd(parentTask.getCmd());
			task.setParentProcess(parentTask);
			parentTask.addChild(task);
		}
		model.addTask(task);
	}

	public void handle_kernel_process_exit(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		// pop the syscall state
		SyscallInfo syscallExit = (SyscallInfo) currentTask.peekState();
		if (syscallExit != null) {
			syscallExit.setEndTime(eventTs);
			currentTask.popState();
		}

		// push the exit state
		ExitInfo exitInfo = (ExitInfo) StateInfoFactory.makeStateInfo(TaskState.EXIT);
		exitInfo.setStartTime(eventTs);
		currentTask.pushState(exitInfo);
	}

	public void handle_net_socket_create(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;
	}

	public void handle_fs_open(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		StateInfo state = currentTask.peekState();
		if (state.getTaskState() != TaskState.SYSCALL)
			return;

		SyscallInfo info = (SyscallInfo) state;
		if (info.getSyscallId() != SymbolTable.SYS_OPEN)
			return;

		String filename = (String) event.parseFieldByName("filename");
		Long fd = (Long) event.parseFieldByName("fd");
		info.setField(Field.FILENAME, filename);
		info.setField(Field.FD, fd.intValue());
	}

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public SystemModel getModel() {
		return this.model;
	}
}
