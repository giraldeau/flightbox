package org.lttng.flightbox.io;

import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniTrace;

public interface ITraceEventHandler extends Comparable<ITraceEventHandler> {
	
	public Set<TraceHook> getHooks();
	
	public void handleInit(TraceReader reader, JniTrace trace);
	
	public void handleComplete(TraceReader reader);

	public Integer getPriority();
}
