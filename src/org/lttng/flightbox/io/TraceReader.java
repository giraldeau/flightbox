package org.lttng.flightbox.io;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;

public class TraceReader {

	String trace_path;
	JniTrace trace; 
	HashMap<EventQuery, TraceEventHandler> handlers;
	HashMap<Class, TraceEventHandler> handlerClassMap;
	
	public TraceReader(String trace_path) {
		this.trace_path = trace_path;
		handlers = new HashMap<EventQuery, TraceEventHandler>();
		handlerClassMap = new HashMap<Class, TraceEventHandler>();
	}
	
	public void loadTrace() throws JniException {
		trace = JniTraceFactory.getJniTrace(trace_path);
	}
	
	public void register(EventQuery query, TraceEventHandler handler) {
		handlers.put(query, handler);
		handlerClassMap.put(handler.getClass(), handler);
	}
	
	public void register(TraceEventHandler handler) {
		handlers.put(new EventQuery(), handler);
		handlerClassMap.put(handler.getClass(), handler);
	}
	
	public void process() throws JniException {
		loadTrace();
		JniEvent event;
		int nbEvents = 0;
		
		for(TraceEventHandler handler: handlers.values()) {
			handler.handleInit(trace);
			handler.setTraceReader(this);
		}
		
		while((event=trace.readNextEvent()) != null) {
			nbEvents++;
			for(EventQuery query: handlers.keySet()) {
				if (query.match(event)) {
					handlers.get(query).handleEvent(event);
				}				
			}
		}
		
		for(TraceEventHandler handler: handlers.values()) {
			handler.handleComplete();
		}
	}
	
	// FIXME: use of generics
	public TraceEventHandler getHandler(Class klass) {
		return handlerClassMap.get(klass);
	}
}
