package org.lttng.flightbox.junit.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.lttng.flightbox.state.StackMachine;
import org.lttng.flightbox.state.StackMachineFactory;
import org.lttng.flightbox.state.TraceEventHandlerState;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.stub.StubTraceReader;
import org.lttng.flightbox.xml.ManifestReader;

public class TestTraceEventHandlerState {

	Map<String, StackMachine> machines;
	
	public static String manifestPath = "/tests/manifest/";
	public static String stubPath = "/tests/stub/";
	public static String dtdPath = "/manifest/";
	
	@Before
	public void testBuildStackMachine() throws JDOMException, IOException {
		String file = System.getenv("project_loc") + manifestPath + "pattern_entry_exit.xml";
		String path = System.getenv("project_loc") + dtdPath;
		
		ManifestReader reader = new ManifestReader();
		Document doc = reader.read(file, path);
		
		XPath xpath = XPath.newInstance("/manifest/stack");
		List<Element> res = (List<Element>) xpath.selectNodes(doc);
		
		machines = new HashMap<String, StackMachine>();
		
		for(Element elem: res) {
			StackMachine machine = StackMachineFactory.fromXml(elem);
			machines.put(machine.getName(), machine);
		}
		assertEquals(2, machines.get("simple").getActions().size());
		assertEquals(4, machines.get("detailed").getActions().size());
		assertEquals(6, machines.get("full").getActions().size());
	}

	
	@Test
	public void testTraceEventHandlerStateSimple() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_simple.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		handler.addAllStackMachine(machines);
		reader.register(handler);
		reader.process();
		Map<String, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(2, objectState.get("simple").size());
		assertEquals(2, objectState.get("detailed").size());
		assertEquals(2, objectState.get("full").size());
		
		assertNull(objectState.get("simple").peek(0L));
		assertEquals("CONNECTED", objectState.get("simple").peek(1L));
		assertEquals("CONNECTED", objectState.get("simple").peek(3L));
		assertNull(objectState.get("simple").peek(6L));
	}
	@Test
	public void testTraceEventHandlerStateSequence() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_sequence.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		handler.addAllStackMachine(machines);
		reader.register(handler);
		reader.process();
		Map<String, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(4, objectState.get("simple").size());
		assertEquals(4, objectState.get("detailed").size());
		assertEquals(4, objectState.get("full").size());
	}
	@Test
	public void testTraceEventHandlerStateNested() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_nested.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		handler.addAllStackMachine(machines);
		reader.register(handler);
		reader.process();
		Map<String, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(2, objectState.get("simple").size());
		assertEquals(4, objectState.get("detailed").size());
		assertEquals(8, objectState.get("full").size());
		assertNull(objectState.get("full").peek(0L));
		assertEquals("CONNECTED", objectState.get("full").peek(1L));
		assertEquals("PROCESSING", objectState.get("full").peek(2L));
		assertEquals("QUERYING", objectState.get("full").peek(3L));
		assertEquals("PROCESSING", objectState.get("full").peek(4L));
		assertEquals("QUERYING", objectState.get("full").peek(5L));
		assertEquals("PROCESSING", objectState.get("full").peek(6L));
		assertEquals("CONNECTED", objectState.get("full").peek(7L));
		assertNull(objectState.get("full").peek(10L));
	}
}
