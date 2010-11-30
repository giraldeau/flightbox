package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTraceReader {
	
	static String trace_dir;
	static public String trace_dir_var = "TRACE_DIR";
	static public String[] trace_names = {"sleep-1x-1sec", "burn-1x-1sec", "burn-8x-1sec"};
	
	@BeforeClass
	public static void setUp() {
		trace_dir = System.getenv(trace_dir_var);
		if (trace_dir == null) {
			throw new RuntimeException("TRACE_DIR not set");
		}
	}
	
	@Test
	public void testLoadSimpleTrace() throws JniException {
		JniTrace trace = JniTraceFactory.getJniTrace(new File(trace_dir, "sleep-1x-1sec").toString());
		Short[] major = {2};
		Short[] minor = {3,5,6};
		ArrayList<Short> major_list = new ArrayList<Short>(Arrays.asList(major));
		ArrayList<Short> minor_list = new ArrayList<Short>(Arrays.asList(minor));
		assertTrue(major_list.contains(trace.getLttMajorVersion()));
		assertTrue(minor_list.contains(trace.getLttMinorVersion()));
	}
}
