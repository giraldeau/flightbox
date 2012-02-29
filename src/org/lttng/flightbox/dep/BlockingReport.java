package org.lttng.flightbox.dep;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.lttng.flightbox.model.RegularFile;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SymbolTable;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;

public class BlockingReport {

	private static String fmt = "%1$-30s%2$12s%3$12s%4$12s%5$12s%6$12s%7$12s\n";
	private static String fmtCpu = "%1$-30s%2$12s%3$12s%4$12s\n";
	private static String fmtMs = "%1$10.3f";
	private static String fmtInt = "%1$10d";

	private static String fmtItem = "%1$-15s%2$15s%3$15s %4$-30s\n";
	private static String fmtTs = "%1$-15s";
	
	public static void printReport(StringBuilder str, Task main, SortedSet<BlockingItem> taskItems, SystemModel model) {
		printReport(str, main, taskItems, model, 0);
	}

	public static void printReport(StringBuilder str, Task main, SortedSet<BlockingItem> taskItems, SystemModel model, int indent) {
		if (taskItems == null || taskItems.isEmpty())
			return;
		
		str.append("Blocking report for task " + main.getCmd() + "[" + main.getProcessId() + "]\n");
		String header = String.format(fmtItem, "Start", "Duration (ms)", "Syscall", "Wakeup");
		str.append(header);
		
		for (BlockingItem item: taskItems) {
			
			String start = String.format(fmtTs, item.getStartTime());
			String duration = String.format(fmtMs, ((double)(item.getEndTime() - item.getStartTime()))/1000000);
			int syscallId = item.getWaitingSyscall().getSyscallId();
			String syscall = String.format("%1$12s", model.getSyscallTable().get(syscallId));
			String wakeup = "";
			if (item.getWakeUp() != null) {
				wakeup = wakeup + item.getWakeUp().toString();
			}
			if (item.getWakeUpTask() != null) {
				Task t = item.getWakeUpTask();
				wakeup = wakeup + " by " + t.getCmd() + "[" + t.getProcessId() + "]";
			}
			str.append(String.format(fmtItem, start, duration, syscall, wakeup));
		}
	}

	public static void printSummary(StringBuilder str, Task task, BlockingStats stats, SystemModel model) {
		if (stats == null || stats.isEmpty())
			return;

		HashMap<Integer, BlockingStatsElement<Integer>> stat = stats.getSyscallStats();
		if (!stat.isEmpty()) {
			SymbolTable sys = model.getSyscallTable();
			str.append("Systemcall blocking summary for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
			String header = String.format(fmt, "Syscall", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)");
			str.append(header);
			drawSep(str, header.length());
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
			String header = String.format(fmt, "FD", "N", "Sum (ms)", "Min (ms)", "Max (ms)", "Mean (ms)", "Stddev (ms)");
			str.append(header);
			drawSep(str, header.length());
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
				
				if (fd instanceof RegularFile) {
					RegularFile file = (RegularFile) fd;
					name = new File(file.getFilename()).getName();
				} else if (elem.getId() instanceof SocketInet) {
					SocketInet sock = (SocketInet) fd;
					name = sock.getIp().toString();
				}
				
				str.append(String.format(fmt, name, nb, sum, min, max, mean, stddev));	
			}
			str.append("\n");
		}
	}
	
	public static void printCpuAccounting(StringBuilder str, Task task, SystemModel model, ResourceUsage<Long> cpuStats) {
		CpuAccountingItem acc = new CpuAccountingItem(task);
		str.append("CPU accounting for task pid=" + task.getProcessId() + " cmd=" + task.getCmd() + "\n");
		String header = String.format(fmtCpu, "PID", "Self (ms)", "Sub (ms)", "Total (ms)");
		str.append(header);
		drawSep(str, header.length());
		printCpuAccounting(str, acc, model, cpuStats, 0);

	}

	private static void printCpuAccounting(StringBuilder str, CpuAccountingItem acc, SystemModel model, ResourceUsage<Long> cpuStats, int i) {
		double selfTime = acc.getSelfTime(model, cpuStats);
		double subTime = acc.getSubtaskTime(model, cpuStats);
		StringBuilder ind = new StringBuilder();
		indent(ind, i);
		ind.append(acc.getTask().getProcessId() + " " + new File(acc.getTask().getCmd()).getName());
		String self = String.format(fmtMs, selfTime/1000000);
		String sub = String.format(fmtMs, subTime/1000000);
		String total = String.format(fmtMs, (subTime + selfTime)/1000000);
		String line = String.format(fmtCpu, ind.toString(), self, sub, total);
		str.append(line);
		TreeSet<CpuAccountingItem> children = acc.getChildren(model, cpuStats);
		for (CpuAccountingItem item: children) {
			printCpuAccounting(str, item, model, cpuStats, i + 1);
		}
	}
	
	private static void indent(StringBuilder str, int indent) {
		for (int i = 1; i < indent; i++) {
			str.append("    ");
		}
		if (indent > 0)
			str.append(" \\_ ");
	}
	
	private static void drawSep(StringBuilder str, int width) {
		for (int i = 1; i < width; i++) {
			str.append("-");
		}
		str.append("\n");
	}
}
