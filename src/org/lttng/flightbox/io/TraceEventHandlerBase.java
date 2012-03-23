package org.lttng.flightbox.io;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniTrace;

public class TraceEventHandlerBase implements ITraceEventHandler {

	private static int autoPriority = 0;
	protected Set<TraceHook> hooks;
	private Integer priority;
	
	public TraceEventHandlerBase(Integer priority) {
		this.hooks = new HashSet<TraceHook>();
		this.priority = priority;
	}
	
	public TraceEventHandlerBase() {
		this(autoPriority++);
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

	@Override
	public Integer getPriority() {
		return priority;
	}
	
	@Override
	public int compareTo(ITraceEventHandler other) {
		return priority.compareTo(other.getPriority());
	}
}
