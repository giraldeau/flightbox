package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

public class TraceEventHandlerCounter extends TraceEventHandlerBase {

	public int count;

	public TraceEventHandlerCounter(Integer priority) {
		super(priority);
		hooks.add(new TraceHook());
	}
	
	public TraceEventHandlerCounter() {
		this(0);
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		count = 0;
	}
	
	public void handle_all_event(TraceReader reader, JniEvent event) {
		count++;
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}
	
	public int getCount() {
		return count;
	}
}
