package org.lttng.flightbox.io;

import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.JniEvent;

public class EventQuery {

	TreeSet<String> eventTypes; // kernel, metadata, etc.
	TreeSet<String> eventNames; // related to the name in JniMarker
	TreeSet<Long> cpuIds;    // 0, 1, 2...
	
	public EventQuery() {
		eventTypes = new TreeSet<String>();
		eventNames = new TreeSet<String>();
		cpuIds = new TreeSet<Long>();
	}
	
	public Boolean match(JniEvent event) {
		Long cpu = new Long(event.getParentTracefile().getCpuNumber());
		String eventType = event.getParentTracefile().getTracefileName();
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();

		if (!eventTypes.isEmpty() && !eventTypes.contains(eventType) ||
			!eventNames.isEmpty() && !eventNames.contains(eventName) ||
			!cpuIds.isEmpty() && !cpuIds.contains(cpu)){
			return false;
		}
		return true;
	}
	
	public void addCpu(Long id) {
		cpuIds.add(id);
	}

	public void addEventName(String name) {
		eventNames.add(name);
	}
	
	public void addEventType(String type) {
		eventTypes.add(type);
	}
}
