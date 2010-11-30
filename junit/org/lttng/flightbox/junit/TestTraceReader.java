package org.lttng.flightbox.junit;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestTraceReader {
	
	String trace_dir;
	static public String trace_dir_var = "TRACE_DIR";
	
	@BeforeClass
	public void setUp() {
		trace_dir = System.getenv(trace_dir_var);
		if (trace_dir == null) {
			throw new RuntimeException("TRACE_DIR not set");
		}
	}
	
	@Test
	public void testLoadSimpleTrace() {
		
	}
}
