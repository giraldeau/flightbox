package org.lttng.flightbox.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerState extends TraceEventHandlerBase {

	Map<Long, VersionizedStack<String>> objectState;
	
	public TraceEventHandlerState() {
		super();
		hooks.add(new TraceHook());
	}

	public void handleInit(TraceReader reader, JniTrace trace) {
		objectState = new HashMap<Long, VersionizedStack<String>>();
	}
	
	public void handle_all_event(TraceReader reader, JniEvent event) {
		
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
		Long eventTs = event.getEventTime().getTime();
		Long connectionId = (Long) event.parseFieldByName("id");
		VersionizedStack<String> state = objectState.get(connectionId);
		if (state == null) {
			state = new VersionizedStack<String>();
			objectState.put(connectionId, state);
		}
		if(eventName.equals("ust_connection_start")) {
			state.push("CONNECTED", eventTs);
		} else if (eventName.equals("ust_connection_done")) {
			state.pop(eventTs);
		}
	}
	public Map<Long, VersionizedStack<String>> getObjectState() {
		return objectState;
	}
}
