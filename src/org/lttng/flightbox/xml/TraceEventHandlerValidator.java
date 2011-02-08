package org.lttng.flightbox.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.xml.TraceEventHandlerValidator.Marker;

/*
 * The purpose of this class is to validate that all trace points required for 
 * an analysis are reported in metadata
 */

public class TraceEventHandlerValidator extends TraceEventHandlerBase {

	public class Marker {
		public String channel;
		public String event;
		public Marker(String channel, String event) {
			this.channel = channel;
			this.event = event;
		}
		public boolean equals(Object other) {
			if (other instanceof Marker) {
				Marker o = (Marker) other;
				return channel.compareTo(o.channel) == 0 && event.compareTo(o.event) == 0;
			}
			return false;
		}
		public int hashCode() {
			return channel.hashCode() + event.hashCode();
		}
		public String toString() {
			return channel + "." + event;
		}
	}
	
	private ArrayList<Marker> markersInventory;
	private Map<Marker, Boolean> markersToFind;
	private boolean isDirty;
	
	public TraceEventHandlerValidator() {
		super();
		hooks.add(new TraceHook("metadata", "core_marker_id"));
		//hooks.add(new TraceHook("metadata", "core_marker_format"));
		markersInventory = new ArrayList<Marker>();
		markersToFind = new HashMap<Marker, Boolean>();
	}
	
	public void addMarker(String channel, String event) {
		markersToFind.put(new Marker(channel, event), false);
		isDirty = true;
	}
	
	public void clearMarker() {
		markersToFind.clear();
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
	}
	
	public void handle_metadata_core_marker_id(TraceReader reader, JniEvent event) {
		String channelName = (String) event.parseFieldByName("channel");
		String eventName = (String) event.parseFieldByName("name");
		markersInventory.add(new Marker(channelName, eventName));
	}
	
	@Override
	public void handleComplete(TraceReader reader) {
		isDirty = true;
	}

	public void updateMarkersCache() {
		if (isDirty) {
			Boolean found;
			for(Marker m1: markersToFind.keySet()) {
				found = false;
				for (Marker m2: markersInventory) {
					if (m1.equals(m2)) {
						found = true;
						break;
					}
				}
				markersToFind.put(m1, found);
			}
			isDirty = false;
		}
	}
	
	public Map<Marker, Boolean> getMarkerStatus() {
		// update the status of markers if inventory has changed
		updateMarkersCache();
		return markersToFind;
	}
	
	public Boolean isValid() {
		updateMarkersCache();
		Boolean res = true;
		for(Boolean m1: markersToFind.values()) {
			if (m1 == false) {
				res = false;
				break;
			}
		}
		return res;
	}
}
