package org.lttng.flightbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.Subgraph;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.dep.BlockingReport;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraph;
import org.lttng.flightbox.graph.ExecGraphProviders;
import org.lttng.flightbox.graph.ExecSubgraph;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.graph.GraphUtils;
import org.lttng.flightbox.graph.TaskGraphExtractor;
import org.lttng.flightbox.io.ITraceEventHandler;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.model.LoggingTaskListener;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.statistics.ResourceUsage;

public class MainDependency {

	static Options options;

	static String[] moduleList = new String[] {"dep", "cp"};
	
	public class CmdOptions {
		public String tracePath;
		public String moduleName;
		public File traceFile;
		public List<String> cmdFilter;
		public List<Integer> pidFilter;
		public Boolean verbose;
		public String dotOutputPrefix;
		public String outputDir;
	}
	
	public static void main(String[] args) throws IOException {
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("t", "trace", true, "trace path");
		options.addOption("c", "cmd", true, "filter by command");
		options.addOption("p", "pid", true, "filter by pid");
		options.addOption("m", "module", true, "analysis module " + Arrays.toString(moduleList));
		options.addOption("o", "output", true, "graph output file");
		options.addOption("d", "outdir", true, "directory to output all graphs");
		options.addOption("v", "verbose", false, "verbose output");

		MainDependency dep = new MainDependency();
		
		CmdOptions opts = dep. new CmdOptions();
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Error parsing arguments");
			printUsage();
			System.exit(1);
		}
		
		processArgs(cmd, opts);
		long t1 = System.currentTimeMillis();
		if (opts.moduleName.equals("cp")) {
			doExecutionGraphAnalysis(opts);
		} else if (opts.moduleName.equals("dep")) {
			doDependencyAnalysis(opts);
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println("Done");
		System.out.println("Analysis time: " + (t2 - t1) + "ms");		
	}

	private static void doExecutionGraphAnalysis(CmdOptions opts) throws IOException {
		SystemModel model = new SystemModel();
		ExecutionTaskListener taskListener = new ExecutionTaskListener();
		model.addTaskListener(taskListener);
		if (opts.verbose) {
			LoggingTaskListener loggingTaskListener = new LoggingTaskListener();
			loggingTaskListener.addAllPid(opts.pidFilter);
			model.addTaskListener(loggingTaskListener);
		}
		TraceEventHandlerProcess handlerProcess = new TraceEventHandlerProcess();
		ITraceEventHandler[] handlers = new ITraceEventHandler[] { handlerProcess };
		
		try {
			ModelBuilder.buildFromTrace(opts.tracePath, model, handlers);
		} catch (JniException e) {
			System.out.println("Error while reading the trace");
			System.out.println(e.getMessage());
		}
		Set<Task> tasks = getFilterTasks(opts.pidFilter, opts.cmdFilter, model);
		
		DOTExporter<ExecVertex, ExecEdge> dotExporter = ExecGraphProviders.getDOTExporter();
		int skipped = 0;
		int processed = 0;
		File dir = new File(opts.outputDir);
		dir.mkdirs();
		ExecGraph execGraph = taskListener.getExecGraph();
		if (execGraph == null) {
			throw new RuntimeException("Execution graph can't be recovered");
		}
		for (Task t: tasks) {
			SortedSet<ExecVertex> taskVertex = taskListener.getTaskVertex(t);
			if (taskVertex == null) {
				skipped++;
				continue;
			}
			ExecSubgraph taskExecGraph = TaskGraphExtractor.getExecutionGraph(execGraph, taskVertex.first(), taskVertex.last());
			if (taskExecGraph == null || taskExecGraph.vertexSet().isEmpty()) {
				if (opts.verbose) {
					System.out.println("skipping pid " + t.getProcessId());
				}
				skipped++;
				continue;
			}
			FileWriter writer = new FileWriter(new File(dir, opts.dotOutputPrefix + t.getProcessId() + ".dot"));
			dotExporter.export(writer, taskExecGraph);
			processed++;
		}
		System.out.println("Tasks skipped: " + skipped);
		System.out.println("Tasks processed: " + processed);
	}

	private static void processArgs(CommandLine cmd, CmdOptions opts) {
		if (cmd.hasOption("help")) {
			printUsage();
			System.exit(0);
		}

		if (cmd.hasOption("trace")) {
			opts.tracePath = cmd.getOptionValue("trace");
		} else {
			printUsage();
			System.exit(1);
		}

		if (cmd.hasOption("pid") && cmd.hasOption("cmd")) {
			printUsage("conflicting options pid and cmd");
			System.exit(1);
		}
		
		if (cmd.hasOption("prefix")) {
			opts.dotOutputPrefix = cmd.getOptionValue("prefix");
		} else {
			opts.dotOutputPrefix = "graph-";
		}
		
		if (cmd.hasOption("outdir")) {
			opts.outputDir = cmd.getOptionValue("outdir");
		} else {
			opts.outputDir = System.getProperty("user.dir");
		}
		opts.pidFilter = new ArrayList<Integer>();
		if (cmd.hasOption("pid")) {
			String str = cmd.getOptionValue("pid");
			String[] lst = str.split(",");
			for (String s: lst) {
				opts.pidFilter.add(Integer.parseInt(s));
			}
		}
		opts.cmdFilter = new ArrayList<String>();
		if (cmd.hasOption("cmd")) {
			String str = cmd.getOptionValue("cmd");
			String[] lst = str.split(",");
			opts.cmdFilter = Arrays.asList(lst);
		}
		opts.verbose = false;
		if (cmd.hasOption("verbose")) {
			opts.verbose = true;
		}

		if (cmd.hasOption("module")) {
			opts.moduleName = cmd.getOptionValue("module");
		} else {
			printUsage("Specify a module");
			System.exit(1);
		}
		
		opts.traceFile = new File(opts.tracePath);
		if (!opts.traceFile.isDirectory() || !opts.traceFile.canRead()) {
			System.out.println("Error: can't read directory " + opts.tracePath);
		}
	}

	public static void doDependencyAnalysis(CmdOptions opts) {
		
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		
		TraceEventHandlerProcess handlerProcess = new TraceEventHandlerProcess();
		ITraceEventHandler[] handlers = new ITraceEventHandler[] { handlerProcess };
		
		try {
			ModelBuilder.buildFromTrace(opts.tracePath, model, handlers);
		} catch (JniException e) {
			System.out.println("Error while reading the trace");
			System.out.println(e.getMessage());
		}
		
		BlockingModel bm = model.getBlockingModel();
		
		StringBuilder str = new StringBuilder();
		Set<Task> tasks = getFilterTasks(opts.pidFilter, opts.cmdFilter, model);
		
		if (opts.verbose) {
			for (Task t: tasks) {
				SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(t);
				BlockingReport.printReport(str, taskItems, model);
			}
		}

		for (Task t: tasks) {
			BlockingStats stats = bm.getBlockingStatsForTask(t);
			BlockingReport.printSummary(str, t, stats, model);
		}

		ResourceUsage<Long> cpuStats = handlerProcess.getUsageStats();
		for (Task t: tasks) {
			BlockingReport.printCpuAccounting(str, t, model, cpuStats);
		}
		
		System.out.println(str.toString());

	}
	
	public static Set<Task> getFilterTasks(List<Integer> pidFilter, List<String> cmdFilter, SystemModel model) {
		TreeSet<Task> tasks = new TreeSet<Task>();
		if (pidFilter == null && cmdFilter == null) {
			for (Set<Task> t: model.getTasks().values()) {
				tasks.addAll(t);
			}
			return tasks;
		}
		
		if (pidFilter != null && !pidFilter.isEmpty()) {
			for (Integer pid: pidFilter) {
				tasks.addAll(model.getTasks().get(pid));
			}
		}
		if (cmdFilter != null && !cmdFilter.isEmpty()) {
			for (String str: cmdFilter) {
				tasks.addAll(model.getTaskByCmdBasename(str));
			}
		}
		return tasks;
	}
	
	private static void printUsage() {
		printUsage("");
	}
	
	private static void printUsage(String string) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("MainDependency", options);
	}

}
