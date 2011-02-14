package org.lttng.flightbox.stub;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;

public class StubJniEvent extends JniEvent {
	
	private HashMap<Integer, JniMarker> markersMap;
	JniTracefile parentTrace;
	static int index = 0;
	String name;
	private int id;
	JniTime eventTime = null;
	Map<String, Object> fieldValues;
	
	public StubJniEvent() {
		markersMap = new HashMap<Integer, JniMarker>();
		eventTime = new JniTime();
		fieldValues = new HashMap<String, Object>();
	}
	
	public int getEventMarkerId() {
		return id;
	}
	
	public HashMap<Integer, JniMarker> getMarkersMap() {
		return markersMap;
	}
	
	public JniTracefile getParentTracefile() {
		return this.parentTrace;
	}
	
	public void setParentTracefile(JniTracefile parentTrace) {
		this.parentTrace = parentTrace;
	}
	
	public void setName(String name) {
		this.name = name;
		for (Integer i: markersMap.keySet()) {
			JniMarker j = markersMap.get(i);
			if(j.getName().equals(name)) {
				id = i; 
				return;
			}
		}
		StubJniMarker marker = new StubJniMarker();
		marker.setName(name);
		id = index++;
		markersMap.put(id, marker);
	}

	public void setFieldValue(String fieldName, String value) {
		fieldValues.put(fieldName, value);
	}
	
	public void setEventTime(Long ts) {
		eventTime.setTime(ts);
	}
	public JniTime getEventTime() {
		return eventTime;
	}
	public Object parseFieldByName(String fieldName) {
		return fieldValues.get(fieldName);
	}
}