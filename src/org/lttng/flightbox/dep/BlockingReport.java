package org.lttng.flightbox.dep;

import java.util.List;

public class BlockingReport {

	public static void printReport(StringBuilder str, List<BlockingItem> items) {
		for (BlockingItem item: items) {
			str.append("pid=" + item.getTask().getProcessId());
			str.append(" cmd=" + item.getTask().getCmd());
			str.append(" start=" + item.getWaitInfo().getStartTime());
			str.append(" end=" + item.getWaitInfo().getEndTime());
			str.append(" wait=" + (item.getWaitInfo().getEndTime() - item.getWaitInfo().getStartTime()));
			str.append(" syscall=" + item.getWaitInfo().getWaitingSyscall().getSyscallId());
			str.append(" wakeup=" + item.getWaitInfo().getWakeUp());
			str.append("\n");
		}
	}

}
