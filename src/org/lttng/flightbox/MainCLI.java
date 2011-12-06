package org.lttng.flightbox;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.Subgraph;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.dep.BlockingReport;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.graph.GraphUtils;
import org.lttng.flightbox.io.ITraceEventHandler;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;

public class MainCLI {

	static Options options;

	public static void main(String[] args) {
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("n", "path", true, "filter by command name");
		options.addOption("p", "pid", true, "filter by pid");
		options.addOption("v", "verbose", false, "verbose output");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Error parsing arguments");
			printUsage();
			System.exit(1);
		}

		System.out.println("args " + Arrays.toString(args) + " " + cmd.getArgList());
		
		List<String> tracePath = null;
		String module = null;
		String cmdFilter = null;
		Integer pidFilter = null;
		boolean hasFilter = false;
		boolean verbose = false;
		
		if (cmd.hasOption("help")) {
			printUsage();
			System.exit(0);
		}

		/* parse the requested module and traces */
		if (cmd.getArgList().size() < 2) {
			printUsage();
			System.exit(1);
		}

		module = (String) cmd.getArgList().remove(0);
		tracePath = (List<String>) cmd.getArgList();

		if (!validateModule(module)) {
			printUsage("Unknown module " + module);
			System.exit(1);
		}
		
		if (!validateTraces(tracePath)) {
			printUsage("Unknown trace");
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
		
		if (cmd.hasOption("verbose")) {
			verbose = true;
		}
		
		hasFilter = (pidFilter != null || cmdFilter != null);
		
		long t1 = System.currentTimeMillis();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		
		ExecutionTaskListener taskListener = new ExecutionTaskListener();
		model.addTaskListener(taskListener);

		TraceEventHandlerProcess handlerProcess = new TraceEventHandlerProcess();
		ITraceEventHandler[] handlers = new ITraceEventHandler[] { handlerProcess };
		
		for (String trace: tracePath) {
			processTrace(trace);
		}
		/*
		try {
			ModelBuilder.buildFromTrace(tracePath, model, handlers);
		} catch (JniException e) {
			System.out.println("Error while reading the trace");
			System.out.println(e.getMessage());
		}
		long t2 = System.currentTimeMillis();

		TreeSet<Task> tasks = new TreeSet<Task>();
		
		if (hasFilter) {
			TreeSet<Task> foundTask = null; 
			if (pidFilter != null) {
				foundTask = model.getTasks().get(pidFilter);
			} else if (cmdFilter != null) {
				foundTask = model.getTaskByCmdBasename(cmdFilter);
			}
			if (foundTask != null && !foundTask.isEmpty()) {
				tasks.addAll(foundTask);
			}
		} else {
			for (TreeSet<Task> set: model.getTasks().values()) {
				tasks.addAll(set);
			}
		}
		
		BlockingModel bm = model.getBlockingModel();
		
		StringBuilder str = new StringBuilder();
		
		if (verbose) {
			for (Task t: tasks) {
				SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(t);
				BlockingReport.printReport(str, taskItems, model);
			}
		}

		for (Task t: tasks) {
			BlockingStats stats = bm.getBlockingStatsForTask(t);
			BlockingReport.printSummary(str, t, stats, model);
		}

		new File("tmp").mkdir();
		
		ResourceUsage<Long> cpuStats = handlerProcess.getUsageStats();
		for (Task t: tasks) {
			BlockingReport.printCpuAccounting(str, t, model, cpuStats);
			Subgraph<ExecVertex, ExecEdge, WeightedGraph<ExecVertex, ExecEdge>> taskExecGraph = taskListener.getTaskExecGraph(t);
			if (taskExecGraph == null)
				continue;
			try {
				GraphUtils.saveGraph(taskExecGraph, String.format("tmp/exec-graph-%s-%d.dot", traceFile.getName(), t.getProcessId()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(str.toString());
		System.out.println("Analysis time: " + (t2 - t1) + "ms");
		*/
		
		System.out.println("Done");
		
	}

	private static boolean validateModule(String module) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean validateTraces(List<String> traces) {
		for (String tracePath: traces) {
			File traceFile = new File(tracePath);
			if (!traceFile.isDirectory() || !traceFile.canRead()) {
				System.out.println("Error: can't read directory " + tracePath);
				return false;
			}
		}
		return true;
	}

	private static void processTrace(String trace) {
		// TODO Auto-generated method stub
		
	}

	private static void printUsage() {
		printUsage("");
	}
	
	private static void printUsage(String string) {
		HelpFormatter formatter = new HelpFormatter();
		System.out.println(string);
		System.out.println("flightbox-cli [options] [trace]");
		System.out.println("Available modules: [ cpa ]");
		formatter.printHelp("MainDependency", options);
	}

}
