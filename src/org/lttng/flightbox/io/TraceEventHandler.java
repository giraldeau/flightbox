package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;

public interface TraceEventHandler {
	
	public void handleInit(JniTrace trace);
	
	public void handleEvent(JniEvent event);
	
	public void handleComplete();
}
