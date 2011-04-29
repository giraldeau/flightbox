package org.lttng.flightbox;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.dep.BlockingReport;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class MainDependency {

	static Options options;

	public static void main(String[] args) {
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("t", "trace", true, "trace path");
		options.addOption("c", "cmd", true, "filter by command");
		options.addOption("p", "pid", true, "filter by pid");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Error parsing arguments");
			printUsage();
			System.exit(1);
		}

		String tracePath = null;
		String cmdFilter = null;
		Integer pidFilter = null;
		boolean hasFilter = false;
		
		if (cmd.hasOption("help")) {
			printUsage();
			System.exit(0);
		}

		if (cmd.hasOption("trace")) {
			tracePath = cmd.getOptionValue("trace");
		} else {
			printUsage();
			System.exit(1);
		}

		if (cmd.hasOption("pid") && cmd.hasOption("cmd")) {
			printUsage("conflicting options pid and cmd");
			System.exit(1);
		}
		if (cmd.hasOption("pid")) {
			pidFilter = Integer.parseInt(cmd.getOptionValue("pid"));
		} else if (cmd.hasOption("cmd")) {
			cmdFilter = cmd.getOptionValue("cmd");
		}
		
		hasFilter = (pidFilter != null || cmdFilter != null);
		
		File traceFile = new File(tracePath);
		if (!traceFile.isDirectory() || !traceFile.canRead()) {
			System.out.println("Error: can't read directory " + tracePath);
			System.exit(1);
		}

		long t1 = System.currentTimeMillis();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		model.addTaskListener(listener);
		listener.setModel(model);

		try {
			ModelBuilder.buildFromTrace(tracePath, model);
		} catch (JniException e) {
			System.out.println("Error while reading the trace");
			System.out.println(e.getMessage());
		}
		long t2 = System.currentTimeMillis();

		HashMap<Integer, TreeSet<Task>> tasks = new HashMap<Integer, TreeSet<Task>>();
		
		if (hasFilter) {
			TreeSet<Task> foundTask = null; 
			if (pidFilter != null) {
				foundTask = model.getTasks().get(pidFilter);
			} else if (cmdFilter != null) {
				foundTask = model.getTaskByCmd(cmdFilter, true);
			}
			if (foundTask != null && !foundTask.isEmpty()) {
				tasks.put(foundTask.first().getProcessId(), foundTask);
			}
		} else {
			tasks = model.getTasks();
		}
		
		BlockingModel bm = model.getBlockingModel();
		
		StringBuilder str = new StringBuilder();
		for(TreeSet<Task> set: tasks.values()) {
			for (Task t: set) {
				SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(t);
				BlockingReport.printReport(str, taskItems, model);
			}
		}

		for(TreeSet<Task> set: tasks.values()) {
			for (Task t: set) {
				BlockingStats stats = bm.getBlockingStatsForTask(t);
				BlockingReport.printSummary(str, t, stats, model);
			}
		}


		System.out.println(str.toString());
		System.out.println("Analysis time: " + (t2 - t1) + "ms");
		System.out.println("Done");
	}

	private static void printUsage() {
		printUsage("");
	}
	
	private static void printUsage(String string) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("MainDependency", options);
	}

}
