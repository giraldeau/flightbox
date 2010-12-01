package org.lttng.flightbox.io;

import java.util.ArrayList;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.lttng.flightbox.TraceVersion;

public class TraceReader {

	String trace_path;
	JniTrace trace; 
	ArrayList<TraceEventHandler> handlers;
	
	public TraceReader(String trace_path) {
		this.trace_path = trace_path;
		handlers = new ArrayList<TraceEventHandler>();
	}
	
	public void loadTrace() throws JniException {
		trace = JniTraceFactory.getJniTrace(trace_path);
	}
	
	public void register(TraceEventHandler handler) {
		handlers.add(handler);
	}
	
	public void process() throws JniException {
		loadTrace();
		JniEvent e;
		int nbEvents = 0;
		while((e=trace.readNextEvent()) != null) {
			nbEvents++;
			for(TraceEventHandler handler: handlers) {
				handler.handleEvent(e);
			}
		}
	}
}
