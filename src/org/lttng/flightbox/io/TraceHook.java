package org.lttng.flightbox.io;

import java.lang.reflect.Method;

public class TraceHook {
	
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

}
