package org.lttng.flightbox.io;

import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

public interface ITraceEventHandler {
	
	public Set<TraceHook> getHooks();
	
	public void handleInit(TraceReader reader, JniTrace trace);
	
	public void handleComplete(TraceReader reader);

}
