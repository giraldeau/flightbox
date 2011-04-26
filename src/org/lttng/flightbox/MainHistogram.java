package org.lttng.flightbox;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.histogram.HistogramPainter;
import org.lttng.flightbox.histogram.IHistogramHandler;
import org.lttng.flightbox.histogram.TraceEventHandlerHistogram;
import org.lttng.flightbox.histogram.TraceEventHandlerHistogramSHT;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceReader;

import statehistory.StateHistorySystem;
import statehistory.common.AttributeNotFoundException;

public class MainHistogram {

	static Options options;

	/**
	 * @param args
	 * @throws ParseException
	 * @throws JniException
	 * @throws IOException
	 * @throws AttributeNotFoundException
	 */
	public static void main(String[] args) throws ParseException, JniException, IOException, AttributeNotFoundException {
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
		File traceDir = null;
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
			traceDir = new File(tracePath);
			if (!traceDir.isDirectory()) {
				throw new IOException("The trace path must be a directory");
			}
		} else {
			printUsage();
			System.exit(1);
		}

		IHistogramHandler handler = null;
		StateHistorySystem shs = null;
		boolean computeHistory = false;
		if (useHistory) {
			File shsFile = getHistoryFile(traceDir);
			computeHistory = !shsFile.exists();
			if (computeHistory) {
				shs = new StateHistorySystem(shsFile.getPath());
			} else {
				shs = new StateHistorySystem(shsFile.getPath(), 0);
			}
			handler = new TraceEventHandlerHistogramSHT();
			handler.setStateHistorySystem(shs);
		} else {
			handler = new TraceEventHandlerHistogram();
			computeHistory = true;
		}

		long t1 = System.currentTimeMillis();
		computeRaw(handler, tracePath, imagePath, imageWidth, computeHistory);
		long t2 = System.currentTimeMillis();

		System.out.println("Total time elapsed " + (t2 - t1));
		System.out.println("Done");

	}

	private static File getHistoryFile(File dir) throws IOException {
		File[] files = dir.listFiles();
		if (files.length == 0) {
			throw new IOException("Trace dir is empty");
		}
		int fileHash = getFileHash(files[0]);
		String home = System.getenv("HOME");
		File cacheDir = new File(home, ".flightbox/cache/");
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return new File(cacheDir, String.format("%d", fileHash) + ".shs");
	}

	private static int getFileHash(File file) {
		long ts = file.lastModified();
		int hc = file.getName().hashCode();
		return Math.abs((int) (ts >>> 32) + hc);
	}
	
	private static void computeRaw(IHistogramHandler handler, String tracePath, String imagePath, int imageWidth, boolean computeHistory) throws JniException, IOException, AttributeNotFoundException {
		long t1 = System.currentTimeMillis();
		handler.setNbSamples(imageWidth);
		if (computeHistory) {
			TraceReader traceReader = new TraceReader(tracePath);
			traceReader.register((TraceEventHandlerBase)handler);
			traceReader.process();
		}
		long t2 = System.currentTimeMillis();
		int[] samples = handler.getSamples();
		long t3 = System.currentTimeMillis();
		HistogramPainter painter = new HistogramPainter();
		painter.paint(samples);
		painter.save(imagePath);
		System.out.println("Computation  : " + (t2 - t1));
		System.out.println("Query samples: " + (t3 - t2));
	}

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "MainHistogram", options );
	}

}
