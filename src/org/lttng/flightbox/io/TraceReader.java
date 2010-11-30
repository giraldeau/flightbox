package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

public class TraceReader {

	String trace_path;
	JniTrace trace; 
	
	public TraceReader(String trace_path) {
		this.trace_path = trace_path;
	}
	
	public void loadTrace() throws JniException {
		trace = JniTraceFactory.getJniTrace(trace_path);
	}
	
	public short getTraceVersion() {
		return trace.getLttMajorVersion();
	}
	
}
