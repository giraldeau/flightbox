package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;

public interface TraceEventHandler {
	
	public void handleEvent(JniEvent event);
	
}
