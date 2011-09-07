package org.lttng.flightbox.model;

import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.AliveInfo;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.IRQInfo;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;
import org.lttng.flightbox.model.state.TrapInfo;
import org.lttng.flightbox.model.state.UserInfo;
import org.lttng.flightbox.model.state.WaitInfo;
import org.lttng.flightbox.model.state.ZombieInfo;

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
		case ALIVE:
			info = new AliveInfo();
		default:
			break;
		}
		if (info != null)
			info.setTaskState(state);
		return info;
	}

}
