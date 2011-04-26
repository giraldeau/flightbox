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
import org.lttng.flightbox.histogram.QueryStateHistory;
import org.lttng.flightbox.histogram.TraceEventHandlerHistogram;
import org.lttng.flightbox.histogram.TraceEventHandlerHistogramSHT;
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
        options.addOption("r", "rebuild", false, "rebuild the state history, don't use cache");
        options.addOption("v", "verbose", false, "verbose mode");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		boolean useHistory = cmd.hasOption("s") || cmd.hasOption("state");
		boolean rebuildHistory = cmd.hasOption("r") || cmd.hasOption("rebuild");
		boolean verbose = cmd.hasOption("v") || cmd.hasOption("verbose");
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

		int[] samples;
		long t1 = 0, t2 = 0, t3 = 0;
		StateHistorySystem shs = null;
		t1 = System.currentTimeMillis();
		if (useHistory) {
		    File shsFile = getHistoryFile(traceDir);
            rebuildHistory = !shsFile.exists() || rebuildHistory;
		    if (rebuildHistory) {
		        computeHistory(shsFile, tracePath);
		    }
		    t2 = System.currentTimeMillis();
		    shs = new StateHistorySystem(shsFile.getPath());
	        samples = computeFromHistoryCache(shsFile, imageWidth, shs);
		} else {
		    t2 = System.currentTimeMillis();
		    samples = computeRaw(tracePath, imageWidth);
		}
		
		t3 = System.currentTimeMillis();
		
		if (shs != null && verbose) {
		    printHistoryTreeStats(shs);
		}
		
        HistogramPainter painter = new HistogramPainter();
        painter.setWidth(imageWidth);
        painter.paint(samples);
        painter.save(imagePath);
        System.out.println("Compute history : " + (t2 - t1));
        System.out.println("Compute samples : " + (t3 - t2));

		System.out.println("Done");

	}

	private static int[] computeRaw(String tracePath, int imageWidth) throws JniException {
        TraceEventHandlerHistogram handler = new TraceEventHandlerHistogram();
        handler.setNbSamples(imageWidth);
        TraceReader traceReader = new TraceReader(tracePath);
        traceReader.register(handler);
        traceReader.process();
        return handler.getSamples();
	}
	
    private static int[] computeFromHistoryCache(File shsFile, int imageWidth, StateHistorySystem shs) throws IOException, AttributeNotFoundException {
        String[] attributePath = TraceEventHandlerHistogramSHT.ATTRIBUTE_PATH;
        int attributeQuark = shs.getAttributeQuark(attributePath);
        return QueryStateHistory.getSamples(shs, attributeQuark, imageWidth);
    }

    private static void computeHistory(File shsFile, String tracePath) throws JniException, IOException {
        TraceReader traceReader = new TraceReader(tracePath);
        traceReader.loadTrace();
        Long startTime = traceReader.getStartTime();
        StateHistorySystem shs = new StateHistorySystem(shsFile.getPath(), startTime);
        TraceEventHandlerHistogramSHT handler = new TraceEventHandlerHistogramSHT();
        handler.setStateHistorySystem(shs);
        traceReader.register(handler);
        traceReader.process();
    }

    private static void printHistoryTreeStats(StateHistorySystem shs) {
        System.out.println(shs.getSHT().toString());
        System.out.println("Average node usage " + shs.getSHT().getAverageNodeUsage());
        System.out.println("File size " + shs.getSHT().getFileSize());
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
	

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "MainHistogram", options );
	}

}
