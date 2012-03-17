package org.lttng.flightbox.io;

import java.lang.reflect.Method;

public class TraceHook implements Comparable<TraceHook> {
	
	public String eventName;
	public String channelName;
	public ITraceEventHandler instance;
	public Method method;
	
	public TraceHook(String channelName, String eventName) {
		this.channelName = channelName; 
		this.eventName = eventName; 
	}
	
	public TraceHook() {
		this(null, null);
	}
	
	public boolean isAllEvent() {
		return this.eventName == null || this.channelName == null;
	}

	public Integer getPriority() {
		if (instance == null)
			return 0;
		return instance.getPriority();
	}

	@Override
	public int compareTo(TraceHook other) {
		return this.getPriority().compareTo(other.getPriority());
	}
	
	@Override
	public String toString() {
		return this.eventName + "." + this.channelName + ":" + getPriority();
	}
	
}
