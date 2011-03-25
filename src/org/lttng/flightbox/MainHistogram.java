package org.lttng.flightbox;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.histogram.HistogramPainter;
import org.lttng.flightbox.histogram.TraceEventHandlerHistogram;
import org.lttng.flightbox.io.TraceReader;

public class MainHistogram {

	static Options options;
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws JniException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, JniException, IOException {
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("s", "state", false, "use state history");
		options.addOption("o", "output", true, "png output file");
		options.addOption("t", "trace", true, "trace path");
		options.addOption("w", "width", true, "histogram width");
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		
		boolean useHistory = cmd.hasOption("s") || cmd.hasOption("state");
		String imagePath = null;
		String tracePath = null;
		int imageWidth = 800;
		
		if (cmd.hasOption("help")) {
			printUsage();
			System.exit(0);
		}

		
		if (cmd.hasOption("width")) {
			imageWidth = Integer.parseInt(cmd.getOptionValue("width"));
		}
		
		if (cmd.hasOption("output")) {
			imagePath = cmd.getOptionValue("output");
		} else {
			imagePath = "output.png";
		}
		
		if (cmd.hasOption("trace")) {
			tracePath = cmd.getOptionValue("trace");
		} else {
			printUsage();
			System.exit(1);
		}

		long t1 = System.currentTimeMillis();
		if (useHistory) {
			computeWithHistory(tracePath, imagePath, imageWidth);
		} else {
			computeRaw(tracePath, imagePath, imageWidth);
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println("Total time elapsed " + (t2 - t1));
		System.out.println("Done");
		
	}

	private static void computeRaw(String tracePath, String imagePath, int imageWidth) throws JniException, IOException {
		long t1 = System.currentTimeMillis();
		TraceEventHandlerHistogram handler = new TraceEventHandlerHistogram();
		handler.setNbSamples(imageWidth);
		TraceReader traceReader = new TraceReader(tracePath);
		traceReader.register(handler);
		traceReader.process();
		long t2 = System.currentTimeMillis();
		int[] samples = handler.getSamples();
		long t3 = System.currentTimeMillis();
		HistogramPainter painter = new HistogramPainter();
		painter.paint(samples);
		painter.save(imagePath);
		for(int x: samples) {
			System.out.print(x + " ");
		}
		System.out.println("Computation  : " + (t2 - t1));
		System.out.println("Query samples: " + (t3 - t2));
	}

	private static void computeWithHistory(String tracePath, String imagePath, int imageWidth) {
		
	}

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "MainHistogram", options );
	}

}
