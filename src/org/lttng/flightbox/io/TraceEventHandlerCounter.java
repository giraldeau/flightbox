package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;

public class TraceEventHandlerCounter implements TraceEventHandler {

	public int count;
	@Override
	public void handleEvent(JniEvent event) {
		count++;
	}
	public int getCount() {
		return count;
	}
	
}
