package org.lttng.flightbox;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.dep.BlockingReport;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.model.SystemModel;

public class MainDependency {

	static Options options;
	
	public static void main(String[] args) {
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("t", "trace", true, "trace path");

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

		File traceFile = new File(tracePath);
		if (!traceFile.isDirectory() || !traceFile.canRead()) {
			System.out.println("Error: can't read directory " + tracePath);
			System.exit(1);
		}

		long t1 = System.currentTimeMillis();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		model.addTaskListener(listener);

		try {
			ModelBuilder.buildFromTrace(tracePath, model);
		} catch (JniException e) {
			System.out.println("Error while reading the trace");
			System.out.println(e.getMessage());
		}

		// output report
		StringBuilder str = new StringBuilder();
		BlockingReport.printReport(str, listener.getBlockingItems());
		System.out.println(str.toString());
		
		long t2 = System.currentTimeMillis();

		System.out.println("Analysis time: " + (t2 - t1) + "ms");
		System.out.println("Done");

	}
	
	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("MainDependency", options);
	}
	
}
