package org.lttng.flightbox.junit.state;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.state.TraceEventHandlerState;
import org.lttng.flightbox.stub.StubTraceReader;

public class TestTraceEventHandlerState {

	@Test
	public void testTraceEventHandlerState() throws JniException {
		String file = System.getenv("project_loc") + "/tests/stub/interval_events_simple.xml";
		StubTraceReader reader = new StubTraceReader(file);
		TraceEventHandlerState handler = new TraceEventHandlerState();
		reader.register(handler);
		reader.process();
	}
}
