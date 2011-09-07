package org.lttng.flightbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.xml.TraceEventHandlerInventory;

/*
 * Simple utility to read metadata and output XML version of events
 */

public class ManifestUtil {
	
	static Options options;
	
	public static void main(String[] args) throws JniException, IOException, JDOMException {
		
		options = new Options();
		options.addOption("h", "help", false, "this help");
		options.addOption("t", "trace", true, "trace path");
		options.addOption("o", "out", true, "output file");
		
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
		String outFile = null;
		
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

		if (cmd.hasOption("out")) {
			outFile = cmd.getOptionValue("out");
		} else {
			printUsage();
			System.exit(1);
		}

		TraceEventHandlerInventory inventory = new TraceEventHandlerInventory();
		TraceReader reader = new TraceReader(tracePath);
		reader.register(inventory);
		reader.process();
		Document inv = inventory.getInventory();
		DocType docType = new DocType("inventory", "inventory.dtd");
		inv.setDocType(docType);
				
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream output = new FileOutputStream(new File(outFile));
		out.output(inv, output);
	}
	
	private static void printUsage() {
		printUsage("");
	}
	
	private static void printUsage(String string) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("MainDependency", options);
	}
}
