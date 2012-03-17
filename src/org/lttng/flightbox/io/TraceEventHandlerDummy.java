package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;

/* This is a dummy class for test purposes */
public class TraceEventHandlerDummy extends TraceEventHandlerCounter {
	public TraceEventHandlerCounter buddy; /* buddy must have greater priority */
	public TraceEventHandlerDummy(Integer priority) {
		super(priority);
	}
	@Override
	public void handle_all_event(TraceReader reader, JniEvent event) {
		if (buddy != null) {
			if (count > buddy.getCount()) {
				throw new RuntimeException("Priority error: current counter must always be higher than buddy counter");
			}
		}
		count++;
	}
	public void setBuddy(TraceEventHandlerCounter handler) {
		this.buddy = handler;
	}
}
