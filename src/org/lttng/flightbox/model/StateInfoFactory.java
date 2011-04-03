package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;

public class StateInfoFactory {

	public static StateInfo makeStateInfo(TaskState state) {
		StateInfo info = null;
		switch(state) {
		case IRQ:
			info = new IRQInfo();
			break;
		case SOFTIRQ:
			info = new SoftIRQInfo();
			break;
		case SYSCALL:
			info = new SyscallInfo();
			break;
		case WAIT:
			info = new WaitInfo();
			break;
		case TRAP:
			info = new TrapInfo();
			break;
		case ZOMBIE:
			info = new ZombieInfo();
			break;
		case USER:
			info = new UserInfo();
			break;
		case EXIT:
			info = new ExitInfo();
			break;
		default:
			break;
		}
		return info;
	}

}
