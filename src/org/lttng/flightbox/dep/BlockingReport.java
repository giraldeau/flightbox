package org.lttng.flightbox.dep;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.lttng.flightbox.model.DiskFile;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class BlockingReport {

	private static String fmt = "%1$25s%2$12s%3$12s%4$12s%5$12s%6$12s%7$12s\n";
	private static String fmtMs = "%1$10.3f";
	private static String fmtInt = "%1$10d";
	
	public static void printReport(StringBuilder str, SortedSet<BlockingItem> taskItems, SystemModel model) {
		printReport(str, taskItems, model, 0);
	}

	public static void printReport(StringBuilder str, SortedSet<BlockingItem> taskItems, SystemModel model, int indent) {
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
			printReport(str, item.getChildren(model), model, indent + 1);
		}
	}

	public static void printSummary(StringBuilder str, Task task, BlockingStats stats, SystemModel model) {
		if (stats == null || stats.isEmpty())
			return;

		HashMap<Integer, BlockingStatsElement<Integer>> stat = stats.getSyscallStats();
		if (!stat.isEmpty()) {
			SymbolTable sys = model.getSyscallTable();
			str.append("Systemcall blocking summary for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
			str.append(String.format(fmt, "Syscall", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)"));
			for (Integer i: stat.keySet()) {
				SummaryStatistics s = stat.get(i).getSummary();
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
		
		HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats = stats.getFileDescriptorStats();
		if (!fdStats.isEmpty()) {
			str.append("File descriptor blocking summary for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
			str.append(String.format(fmt, "FD", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)"));
			for (FileDescriptor i: fdStats.keySet()) {
				BlockingStatsElement<FileDescriptor> elem = fdStats.get(i);
				if (elem == null)
					continue;
				FileDescriptor fd = elem.getId();
				SummaryStatistics s = elem.getSummary();
				String nb = String.format(fmtInt, s.getN());
				String sum = String.format(fmtMs, s.getSum()/1000000);
				String min = String.format(fmtMs, s.getMin()/1000000);
				String max = String.format(fmtMs, s.getMax()/1000000);
				String mean = String.format(fmtMs, s.getMean()/1000000);
				String stddev = String.format(fmtMs, s.getStandardDeviation()/1000000);
				String name = null;
				
				if (fd instanceof DiskFile) {
					DiskFile file = (DiskFile) fd;
					name = new File(file.getFilename()).getName();
				} else if (elem.getId() instanceof SocketInet) {
					SocketInet sock = (SocketInet) fd;
					name = SocketInet.formatIPv4(sock.getDstAddr()) + ":" + sock.getDstPort();
				}
				
				str.append(String.format(fmt, name, nb, sum, min, max, mean, stddev));	
			}
			str.append("\n");
		}
	}
	
}
