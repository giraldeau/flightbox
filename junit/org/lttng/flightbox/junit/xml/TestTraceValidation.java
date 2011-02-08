package org.lttng.flightbox.junit.xml;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.xml.TraceEventHandlerValidator;

public class TestTraceValidation {

	static String trace_dir;
	static public String trace_dir_var = "TRACE_DIR";
	
	@BeforeClass
	public static void setUp() {
		trace_dir = System.getenv(trace_dir_var);
		if (trace_dir == null) {
			throw new RuntimeException("TRACE_DIR not set");
		}
	}
	
	@Test
	public void testTraceValidateLinuxPass() throws JniException {
		TraceReader reader = new TraceReader(trace_dir + "sleep-1x-1sec");
		TraceEventHandlerValidator validator = new TraceEventHandlerValidator();
		reader.register(validator);
		reader.process();
		validator.clearMarker();
		validator.addMarker("kernel", "foo");
		assertFalse(validator.isValid());
		validator.clearMarker();
		validator.addMarker("foo", "sched_schedule");
		assertFalse(validator.isValid());
		validator.clearMarker();
		validator.addMarker("foo", "bar");
		assertFalse(validator.isValid());
		validator.clearMarker();
		validator.addMarker("kernel", "sched_schedule");
		assertTrue(validator.isValid());
	}
}
