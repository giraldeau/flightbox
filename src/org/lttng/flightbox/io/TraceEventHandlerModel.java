package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.StateInfoFactory;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.IRQInfo;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;
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
		hooks.add(new TraceHook("fs", "exec"));
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
		if (info == null)
			return;

		if (info.getTaskState() == TaskState.SYSCALL) {
			SyscallInfo state = (SyscallInfo) info;
			state.setEndTime(eventTs);
			state.setRetCode(syscallRet.intValue());
			currentTask.popState();
		} else {
			// add warning
		}
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
		if (info == null)
			return;

		if (info.getTaskState() == TaskState.IRQ) {
			IRQInfo state = (IRQInfo) info;
			state.setEndTime(eventTs);
			currentTask.popState();
		} else {
			// add warning, got the exit without the entry
		}
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
		if (info == null)
			return;

		if (info.getTaskState() == TaskState.SOFTIRQ) {
			SoftIRQInfo state = (SoftIRQInfo) info;
			state.setEndTime(eventTs);
			currentTask.popState();
		} else {
			// add warning, got the exit without the entry
		}
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

		// update tasks state
		StateInfo infoNext = nextTask.peekState();
		if (infoNext != null)
			infoNext.setEndTime(eventTs);
		nextTask.popState();

		StateInfo prevInfo = prevTask.peekState();
		if (prevInfo != null) {
			if (prevInfo instanceof ExitInfo) {
				// last schedule of the process
				ExitInfo exit = (ExitInfo) prevInfo;
				exit.setEndTime(eventTs);
				prevTask.setExitTime(eventTs);
				prevTask.popState();
			} else {
				WaitInfo waitInfoPrev = (WaitInfo) StateInfoFactory.makeStateInfo(TaskState.WAIT);
				waitInfoPrev.setStartTime(eventTs);
				prevTask.pushState(waitInfoPrev);
			}
		}
	}

	public void handle_kernel_sched_try_wakeup(TraceReader reader, JniEvent event) {
		Long cpu = event.getParentTracefile().getCpuNumber();
		long eventTs = event.getEventTime().getTime();
		Long pid = (Long) event.parseFieldByName("pid");
		Task wakedTask = model.getLatestTaskByPID(pid.intValue());
		Task currentTask = model.getProcessors().get(cpu.intValue()).getCurrentTask();
		StateInfo info = wakedTask.peekState();
		StateInfo wakeCause = null;
		if (currentTask != null)
			wakeCause = currentTask.peekState();

		/*
		 * try_wakeup can happen on already running task
		 * make sure the task was waiting
		 */
		if (info instanceof WaitInfo) {
			WaitInfo wait = (WaitInfo) info;
			StateInfo waitInState = wakedTask.peekState(-1);
			if (waitInState.getTaskState() != TaskState.SYSCALL) {
				waitInState = null;
			}
			wait.setBlocking(true);
			wait.setEndTime(eventTs);
			wait.setWakeUp(wakeCause);
			wait.setWaitingSyscall((SyscallInfo)waitInState);
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
		task.setCreateTime(eventTs);
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

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public SystemModel getModel() {
		return this.model;
	}
}
