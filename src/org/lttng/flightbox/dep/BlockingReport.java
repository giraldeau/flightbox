package org.lttng.flightbox.dep;

import java.util.List;

import org.lttng.flightbox.model.SystemModel;

public class BlockingReport {

	public static void printReport(StringBuilder str, List<BlockingItem> items, SystemModel model) {
		for (BlockingItem item: items) {
			str.append("pid=" + item.getTask().getProcessId());
			str.append(" cmd=" + item.getTask().getCmd());
			str.append(" wait=" + (item.getWaitInfo().getEndTime() - item.getWaitInfo().getStartTime()));
			int syscallId = item.getWaitInfo().getWaitingSyscall().getSyscallId();
			str.append(" syscall=" + model.getSyscallTable().get(syscallId));
			str.append(" wakeup=" + item.getWaitInfo().getWakeUp());
			str.append("\n");
		}
	}

}
