package org.lttng.flightbox.dep;

import java.util.HashMap;
import java.util.SortedSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

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
			str.append(" start=" + item.getStartTime());
			str.append(" end=" + item.getEndTime());
			str.append(" wait=" + (item.getEndTime() - item.getStartTime())/1000000 + "ms");
			int syscallId = item.getWaitingSyscall().getSyscallId();
			str.append(" syscall=" + model.getSyscallTable().get(syscallId));
			if (item.getWakeUp() != null) {
				str.append(" wakeup=" + item.getWakeUp().toString());
			}
			if (item.getWakeUpTask() != null) {
				str.append(" wakeup=" + item.getWakeUpTask().getCmd());
			}
			str.append("\n");
			printReport(str, item.getChildren(), model, indent + 1);
		}
	}

	public static void printSummary(StringBuilder str, Task task, BlockingStats stats, SystemModel model) {
		if (stats == null) {
			return;
		}
		HashMap<Integer, SummaryStatistics> stat = stats.getSyscallStats();
		SymbolTable sys = model.getSyscallTable();
		str.append("Summary for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
		str.append(String.format("%1$20s%2$12s%3$12s%4$12s\n", "Syscall", "Sum (ms)", "Mean (ms)", "Stddev (ms)"));
		for (Integer i: stat.keySet()) {
			SummaryStatistics s = stat.get(i);
			String sum = String.format("%1$10.3f", s.getSum()/1000000);
			String mean = String.format("%1$10.3f", s.getMean()/1000000);
			String stddev = String.format("%1$10.3f", s.getStandardDeviation()/1000000);
			str.append(String.format("%1$20s%2$12s%3$12s%4$12s\n", sys.get(i), sum, mean, stddev));
		}
		str.append("\n");
	}

}
