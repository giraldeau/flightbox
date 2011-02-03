package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

public class TraceEventHandlerCounter implements TraceEventHandler {

	public int count;
	private TraceReader traceReader;
	
	@Override
	public void handleInit(JniTrace trace) {
		count = 0;
	}
	
	@Override
	public void handleEvent(JniEvent event) {
		count++;
	}

	@Override
	public void handleComplete() {
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	public void setTraceReader(TraceReader traceReader) {
		this.traceReader = traceReader;
	}
}
