package org.lttng.flightbox.io;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;

public class EventData {

	long ts; 
	long cpu; 
	String type;
	String name; 
	HashMap<String, Object> values;
	
	public EventData(){
		values = new HashMap<String, Object>();
	}
	
	public void update(JniEvent event) {
		values.clear();
		JniMarker markers = event.getMarkersMap().get(event.getEventMarkerId());
		for (String fieldName: markers.getMarkerFieldsHashMap().keySet()){
			Object o = event.parseFieldByName(fieldName);
			values.put(fieldName, o);
		}
		ts = event.getEventTime().getTime();
		cpu = event.getParentTracefile().getCpuNumber();
		name = markers.getName();
		type = event.getParentTracefile().getTracefileName();
	}
	
	public Long getTime() {
		return ts;
	}
	public void setTime(Long ts) {
		this.ts = ts;
	}
	public Long getCpu() {
		return cpu;
	}
	public void setCpu(Long cpu) {
		this.cpu = cpu;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Object> getValues() {
		return values;
	}
	public void setValues(HashMap<String, Object> values) {
		this.values = values;
	}
	public Object get(String key) {
		return this.values.get(key);
	}
	
}
