package org.lttng.flightbox.junit.state;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.state.TraceEventHandlerState;
import org.lttng.flightbox.state.VersionizedStack;
import org.lttng.flightbox.stub.StubTraceReader;

public class TestTraceEventHandlerState {

	@Test
	public void testTraceEventHandlerStateSimple() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_simple.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		reader.register(handler);
		reader.process();
		Map<Long, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(1, objectState.keySet().size());
		assertEquals(2, objectState.get(0L).size());
	}
	@Test
	public void testTraceEventHandlerStateSequence() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_sequence.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		reader.register(handler);
		reader.process();
		Map<Long, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(1, objectState.keySet().size());
		assertEquals(4, objectState.get(0L).size());
	}
	@Test
	public void testTraceEventHandlerStateMixed() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_mixed.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		reader.register(handler);
		reader.process();
		Map<Long, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(2, objectState.keySet().size());
		assertEquals(2, objectState.get(0L).size());
		assertEquals(2, objectState.get(1L).size());
	}
	@Test
	public void testTraceEventHandlerStateNested() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_nested.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		reader.register(handler);
		reader.process();
		Map<Long, VersionizedStack<String>> objectState = handler.getObjectState();
		assertEquals(1, objectState.keySet().size());
		assertEquals(4, objectState.get(0L).size());
	}
}
