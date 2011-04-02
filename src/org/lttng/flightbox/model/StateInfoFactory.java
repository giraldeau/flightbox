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
		case TRAP:
		case ZOMBIE:
		case READY:
		case USER:
		case EXIT:
			break;
		default:
			break;
		}
		return info;
	}

}
