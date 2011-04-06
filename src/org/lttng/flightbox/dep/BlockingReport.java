package org.lttng.flightbox.dep;

import java.util.SortedSet;

import org.lttng.flightbox.model.SystemModel;

public class BlockingReport {

	public static void printReport(StringBuilder str, SortedSet<BlockingTree> taskItems, SystemModel model) {
		printReport(str, taskItems, model, 0);
	}

	public static void printReport(StringBuilder str, SortedSet<BlockingTree> taskItems, SystemModel model, int indent) {
		if (taskItems == null)
			return;

		for (BlockingTree item: taskItems) {
			for (int i = 1; i < indent; i++) {
				str.append("    ");
			}
			if (indent > 0)
				str.append(" \\_ ");

			str.append("pid=" + item.getTask().getProcessId());
			str.append(" cmd=" + item.getTask().getCmd());
			//str.append(" start=" + item.getStartTime());
			//str.append(" end=" + item.getEndTime());
			str.append(" wait=" + (item.getEndTime() - item.getStartTime())/1000000 + "ms");
			int syscallId = item.getWaitingSyscall().getSyscallId();
			str.append(" syscall=" + model.getSyscallTable().get(syscallId));
			str.append(" wakeup=" + item.getWakeUp().toString());
			str.append("\n");
			printReport(str, item.getChildren(), model, indent + 1);
		}
	}

}
