package org.lttng.flightbox.junit.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.xml.ManifestReader;
import org.lttng.flightbox.xml.MarkerSetOperations;
import org.lttng.flightbox.xml.TraceEventHandlerInventory;

public class TestTraceValidation {

	@Test
	public void testTraceValidateLinuxPass() throws JniException, JDOMException {
		File file = new File(Path.getTraceDir(), "sleep-1x-1sec");
		TraceReader reader = new TraceReader(file.getPath());
		TraceEventHandlerInventory handler = new TraceEventHandlerInventory();
		reader.register(handler);
		reader.process();
		Document inventory = handler.getInventory();
		XPath xpath = XPath.newInstance("//channel");
		List<Element> channels = (List<Element>) xpath.selectNodes(inventory);
		
		// ltt-armall enable 22 channels by default
		// FIXME: on 32 bits, there are only 21 channels?
		assertTrue(22 == channels.size() || 21 == channels.size());
		
		/*
		XMLOutputter out = new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
		try {
			out.output(inventory, System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
	}

	@Test
	public void testReadAndValidatePassSleep1() throws JDOMException, IOException, JniException {
		File filePass = new File(Path.getTestManifestDir(), "linux_pass_metadata.xml");
		File fileFail1 = new File(Path.getTestManifestDir(), "linux_fail_metadata_unknown_channel.xml");
		File fileFail2 = new File(Path.getTestManifestDir(), "linux_fail_metadata_unknown_event.xml");
		File fileFail3 = new File(Path.getTestManifestDir(), "linux_fail_metadata_unknown_field.xml");
		ManifestReader manifestReader = new ManifestReader();
		Document manifestPass = manifestReader.read(filePass.getPath());
		Document manifestFail1 = manifestReader.read(fileFail1.getPath());
		Document manifestFail2 = manifestReader.read(fileFail2.getPath());
		Document manifestFail3 = manifestReader.read(fileFail3.getPath());
		
		File file = new File(Path.getTraceDir(), "sleep-1x-1sec");
		TraceReader reader = new TraceReader(file.getPath());
		TraceEventHandlerInventory handler = new TraceEventHandlerInventory();
		reader.register(handler);
		reader.process();
		
		Document inventory = handler.getInventory();
		
		List<Element> missing;
		missing = MarkerSetOperations.containsAll(inventory, manifestPass);
		assertEquals(missing.size(), 0);
		missing = MarkerSetOperations.containsAll(inventory, manifestFail1);
		assertEquals(missing.size(), 1);
		missing = MarkerSetOperations.containsAll(inventory, manifestFail2);
		assertEquals(missing.size(), 1);
		missing = MarkerSetOperations.containsAll(inventory, manifestFail3);
		assertEquals(missing.size(), 1);
	}
	
}
