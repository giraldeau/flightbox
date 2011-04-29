package org.lttng.flightbox.dep;

import java.util.HashMap;
import java.util.SortedSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class BlockingReport {

	private static String fmt = "%1$20s%2$12s%3$12s%4$12s%5$12s%6$12s%7$12s\n";
	private static String fmtMs = "%1$10.3f";
	private static String fmtInt = "%1$10d";
	
	public static void printReport(StringBuilder str, SortedSet<BlockingItem> taskItems, SystemModel model, BlockingModel blockingModel) {
		printReport(str, taskItems, model, blockingModel, 0);
	}

	public static void printReport(StringBuilder str, SortedSet<BlockingItem> taskItems, SystemModel model, BlockingModel blockingModel, int indent) {
		if (taskItems == null || taskItems.isEmpty())
			return;

		for (BlockingItem item: taskItems) {
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
			printReport(str, item.getChildren(blockingModel), model, blockingModel, indent + 1);
		}
	}

	public static void printSummary(StringBuilder str, Task task, BlockingStats stats, SystemModel model) {
		if (stats == null || stats.isEmpty())
			return;

		HashMap<Integer, SummaryStatistics> stat = stats.getSyscallStats();
		SymbolTable sys = model.getSyscallTable();
		str.append("Summary for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
		str.append(String.format(fmt, "Syscall", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)"));
		for (Integer i: stat.keySet()) {
			SummaryStatistics s = stat.get(i);
			String nb = String.format(fmtInt, s.getN());
			String sum = String.format(fmtMs, s.getSum()/1000000);
			String min = String.format(fmtMs, s.getMin()/1000000);
			String max = String.format(fmtMs, s.getMax()/1000000);
			String mean = String.format(fmtMs, s.getMean()/1000000);
			String stddev = String.format(fmtMs, s.getStandardDeviation()/1000000);
			str.append(String.format(fmt, sys.get(i), nb, sum, min, max, mean, stddev));
		}
		str.append("\n");
	}

}
