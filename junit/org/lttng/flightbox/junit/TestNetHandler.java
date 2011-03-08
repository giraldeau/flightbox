package org.lttng.flightbox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.net.TraceEventHandlerNet;

public class TestNetHandler {

	@Test
	public void testNetHandler() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "net-simple").toString();
		TraceEventHandlerProcess handlerProc = new TraceEventHandlerProcess();
		TraceEventHandlerNet handlerNet = new TraceEventHandlerNet();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(handlerProc);
		reader.register(handlerNet);
		reader.process();
	}
}
