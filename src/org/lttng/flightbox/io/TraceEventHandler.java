package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

public interface TraceEventHandler {
	
	public void handleInit(JniTrace trace);
	
	public void handleEvent(JniEvent event);
	
	public void handleComplete();

	// FIXME: this is bad. First would need to add the associated code 
	// in base class to avoid code duplication. In general, what would be the best way to
	// request reference on the traceReader and retrieve the currentProcess(cpu)?
	public void setTraceReader(TraceReader traceReader);
}
