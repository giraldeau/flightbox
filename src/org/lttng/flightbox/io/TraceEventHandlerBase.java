package org.lttng.flightbox.io;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniTrace;

public class TraceEventHandlerBase implements ITraceEventHandler {

	protected Set<TraceHook> hooks;

	public TraceEventHandlerBase() {
		hooks = new HashSet<TraceHook>();
	}

	@Override
	public Set<TraceHook> getHooks() {
		return hooks;
	}

	public void setHooks(Set<TraceHook> hooks) {
		this.hooks = hooks;
	}

	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {

	}

	@Override
	public void handleComplete(TraceReader reader) {

	}
}
