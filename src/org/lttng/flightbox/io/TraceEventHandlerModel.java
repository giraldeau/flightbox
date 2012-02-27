package org.lttng.flightbox.io;

import java.util.List;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.lttng.flightbox.model.RegularFile;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.StateInfoFactory;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.AliveInfo;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.IRQInfo;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.StateInfo.Field;
import org.lttng.flightbox.model.state.SyscallInfo;
import org.lttng.flightbox.model.state.WaitInfo;

import com.rits.cloning.Cloner;

public class TraceEventHandlerModel extends TraceEventHandlerBase {

	private SystemModel model;
	private static final Cloner cloner = new Cloner();
	
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
		hooks.add(new TraceHook("net", "socket_bind"));
		hooks.add(new TraceHook("net", "socket_connect"));
		hooks.add(new TraceHook("net", "socket_accept"));
		hooks.add(new TraceHook("net", "socket_shutdown"));
		hooks.add(new TraceHook("net", "socket_connect_inet"));
		hooks.add(new TraceHook("net", "socket_accept_inet"));
		hooks.add(new TraceHook("net", "socket_recvmsg"));
		hooks.add(new TraceHook("net", "socket_sendmsg"));
		hooks.add(new TraceHook("fs", "exec"));
		hooks.add(new TraceHook("fs", "open"));
		hooks.add(new TraceHook("fs", "read"));
		hooks.add(new TraceHook("fs", "select"));
		hooks.add(new TraceHook("fs", "write"));
		hooks.add(new TraceHook("fs", "close"));
		
		/* dont clone task class because causes recursive cloning */
		cloner.dontClone(Task.class);
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

		mergeSocketInfo(state);
		
		currentTask.popState();
	}

	private void mergeSocketInfo(SyscallInfo state) {
		if (state.getField(Field.SOCKET) == null)
			return;
		
		SocketInet sock = (SocketInet) state.getField(Field.SOCKET);
		if (!sock.isSet()) {
			if (state.getField(Field.DST_ADDR) != null)
				sock.setDstAddr((Long)state.getField(Field.DST_ADDR));
			if (state.getField(Field.SRC_ADDR) != null)
				sock.setSrcAddr((Long)state.getField(Field.SRC_ADDR));
			if (state.getField(Field.DST_PORT) != null)
				sock.setDstPort((Integer)state.getField(Field.DST_PORT));
			if (state.getField(Field.SRC_PORT) != null)
				sock.setSrcPort((Integer)state.getField(Field.SRC_PORT));
			if (state.getField(Field.IS_CLIENT) != null)
				sock.setClient((Boolean)state.getField(Field.IS_CLIENT));
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
		if (infoNext != null && infoNext.getTaskState() == TaskState.WAIT) {
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
			// remove alive state
			AliveInfo alive = (AliveInfo) prevTask.peekState();
			alive.setEndTime(eventTs);
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
		if (currentTask != null) {
			wakeCause = currentTask.peekState();
		}

		if (wakeCause != null) {
			// FIXME: is it the best way to handle multiple wakeup for a task?
			if (wakeCause.getTaskState() == TaskState.SOFTIRQ) {
				wakedTask.setLastWakeUp(wakeCause);
			}
		}

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
			if (currentTask != null && currentTask.isKernelThread() && wakeCause == null) {
				wait.setWakeUpTask(currentTask);
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
		StateInfo info = StateInfoFactory.makeStateInfo(TaskState.ALIVE);
		info.setStartTime(eventTs);
		model.addTask(task);
		task.setParentTask(parentTask);
		task.pushState(info);
		if (parentTask != null) {
			task.setCmd(parentTask.getCmd());
			parentTask.addChild(task);
			task.setParentTask(parentTask);
			List<FileDescriptor> openedFd = parentTask.getOpenedFileDescriptors();
			task.addFileDescriptors(cloner.deepClone(openedFd));
			// disable listeners while setting the state of the new task
			task.setEnableListeners(false);
			for (StateInfo state :parentTask.getStates()) {
				if (state instanceof AliveInfo)
					continue;
				task.pushState(cloner.deepClone(state));
			}
			task.setEnableListeners(true);
		}
	}
	
	public void handle_kernel_process_exit(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		// pop the syscall state
		StateInfo state = currentTask.peekState();
		if (state != null && (state instanceof SyscallInfo)) {
			SyscallInfo syscallExit = (SyscallInfo) state;
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

		Long family = (Long) event.parseFieldByName("family");
		Long type = (Long) event.parseFieldByName("type");
		Long protocol = (Long) event.parseFieldByName("protocol");
		Long ret = (Long) event.parseFieldByName("ret");
		Jni_C_Pointer pointer = (Jni_C_Pointer) event.parseFieldByName("sock");
		
		if (ret < 0)
			return;

		if (family == SocketInet.AF_INET) {
			SocketInet s = new SocketInet();
			s.setFd(ret.intValue());
			s.setOwner(currentTask);
			s.setFamily(family.intValue());
			s.setType(type.intValue());
			s.setProtocol(protocol.intValue());
			s.setPointer(pointer.getPointer());
			s.setStartTime(eventTs);
			currentTask.addFileDescriptor(s);
		}
	}

	public void handle_net_socket_bind(TraceReader reader, JniEvent event) {
	}

	public void handle_net_socket_connect(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		StateInfo state = currentTask.peekState();
		if (state == null || state.getTaskState() != TaskState.SYSCALL)
			return;

		Long fd = (Long) event.parseFieldByName("fd");
		FileDescriptor file = currentTask.getLatestFileDescriptor(fd.intValue());
		
		if (file == null || !(file instanceof SocketInet))
			return;

		SocketInet sock = (SocketInet) file;
		sock.setOwner(currentTask);
		state.setField(Field.SOCKET, sock);
	}

	public void handle_net_socket_accept(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		StateInfo state = currentTask.peekState();
		if (state == null || state.getTaskState() != TaskState.SYSCALL)
			return;

		Long ret = (Long) event.parseFieldByName("ret");
		Jni_C_Pointer pointer = (Jni_C_Pointer) event.parseFieldByName("sock");

		SocketInet sock = new SocketInet();
		sock.setFd(ret.intValue());
		sock.setPointer(pointer.getPointer());
		sock.setOwner(currentTask);
		sock.setStartTime(eventTs);
		currentTask.addFileDescriptor(sock);
		state.setField(Field.SOCKET, sock);
	}

	public void handle_net_socket_shutdown(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long fd = (Long) event.parseFieldByName("fd");
		Long ret = (Long) event.parseFieldByName("ret");

		if (ret != 0)
			return;

		FileDescriptor file = currentTask.getLatestFileDescriptor(fd.intValue());
		if (file == null || !(file instanceof SocketInet))
			return;

		SocketInet sock = (SocketInet) file;
		sock.setEndTime(eventTs);

	}

	public void handle_net_socket_connect_inet(TraceReader reader, JniEvent event) {
		handle_net_common(reader, event, true);
	}
	
	public void handle_net_socket_accept_inet(TraceReader reader, JniEvent event) {
		handle_net_common(reader, event, false);
	}

	public void handle_net_common(TraceReader reader, JniEvent event, Boolean isClient) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;
		StateInfo info = currentTask.peekState();
		TaskState type = info.getTaskState();
		if (type != TaskState.SYSCALL)
			return;
		
		Long x;
		x = (Long) event.parseFieldByName("saddr");
		info.setField(Field.SRC_ADDR, x);
		x = (Long) event.parseFieldByName("sport");
		info.setField(Field.SRC_PORT, x.intValue());
		x = (Long) event.parseFieldByName("daddr");
		info.setField(Field.DST_ADDR, x);
		x = (Long) event.parseFieldByName("dport");
		info.setField(Field.DST_PORT, x.intValue());
		info.setField(Field.IS_CLIENT, isClient);
	}
	
	public void handle_net_socket_sendmsg(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;
		StateInfo info = currentTask.peekState();
		TaskState type = info.getTaskState();
		if (type != TaskState.SYSCALL)
			return;
		
		/* assumption: send always occurs before recv */
		Jni_C_Pointer pointer = (Jni_C_Pointer) event.parseFieldByName("sock");
		Long size = (Long) event.parseFieldByName("size");
		SocketInet sock = currentTask.getSocketByPointer(pointer.getPointer());
		if (sock == null)
			return;
		
		sock.incrementSend(size);
	}

	public void handle_net_socket_recvmsg(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;
		StateInfo info = currentTask.peekState();
		if (info == null)
			return;
		TaskState type = info.getTaskState();
		if (type != TaskState.SYSCALL)
			return;
		
		Jni_C_Pointer pointer = (Jni_C_Pointer) event.parseFieldByName("sock");
		Long size = (Long) event.parseFieldByName("size");
		SocketInet sock = currentTask.getSocketByPointer(pointer.getPointer());
		if (sock == null)
			return;
		
		sock.incrementRecv(size);		
	}
	
	public void handle_fs_open(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		String filename = (String) event.parseFieldByName("filename");
		Long fd = (Long) event.parseFieldByName("fd");
		RegularFile file = new RegularFile();
		file.setFd(fd.intValue());
		file.setFilename(filename);
		file.setStartTime(eventTs);
		file.setOwner(currentTask);
		if (fd < 0) {
			file.setEndTime(eventTs);
			file.setError(true);
		}
		currentTask.addFileDescriptor(file);
		StateInfo state = currentTask.peekState();
		if (state == null || state.getTaskState() != TaskState.SYSCALL)
			return;
		SyscallInfo info = (SyscallInfo) state;
		info.setFileDescriptor(file);
	}

	public void handle_fs_read(TraceReader reader, JniEvent event) {
		handle_fs_generic(reader, event, true);
	}

	public void handle_fs_write(TraceReader reader, JniEvent event) {
		handle_fs_generic(reader, event, false);
	}

	public void handle_fs_select(TraceReader reader, JniEvent event) {
		handle_fs_generic(reader, event, false);
	}
	
	public void handle_fs_generic(TraceReader reader, JniEvent event, boolean isRead) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;
		
		Long fd = (Long) event.parseFieldByName("fd");
		
		FileDescriptor file = currentTask.getLatestFileDescriptor(fd.intValue());
		if (file == null)
			return;
		file.setOwner(currentTask);
		/*
		if (isRead) {
			file.incrementRead(count.intValue());
		} else {
			file.incrementWrite(count.intValue())
		}
		*/
		StateInfo state = currentTask.peekState();
		if (state == null || state.getTaskState() != TaskState.SYSCALL)
			return;
		SyscallInfo info = (SyscallInfo) state;
		info.setFileDescriptor(file);
		
	}
	
	public void handle_fs_close(TraceReader reader, JniEvent event) {
		long eventTs = event.getEventTime().getTime();
		Long cpu = event.getParentTracefile().getCpuNumber();
		Processor p = model.getProcessors().get(cpu.intValue());
		Task currentTask = p.getCurrentTask();
		if (currentTask == null)
			return;

		Long fd = (Long) event.parseFieldByName("fd");
		FileDescriptor file = currentTask.getLatestFileDescriptor(fd.intValue());
		if (file == null)
			return;

		file.setOwner(currentTask);
		file.setEndTime(eventTs);
		
		StateInfo state = currentTask.peekState();
		if (state == null || state.getTaskState() != TaskState.SYSCALL)
			return;
		SyscallInfo info = (SyscallInfo) state;
		info.setFileDescriptor(file);
	}

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public SystemModel getModel() {
		return this.model;
	}
}
