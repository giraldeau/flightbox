package org.lttng.flightbox.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class StubJniTrace extends JniTrace {

	private List<Element> events;
	private int pos;
	StubJniEvent event;
	Map<String, Class> fieldMap;
	Map<String, ArrayList<String>> eventMap;
	Map<String, ArrayList<String>> channelMap;
	private int cpu;
	
	public StubJniTrace() {
		 event = new StubJniEvent();
		 event.setParentTracefile(new StubJniTracefile());
		 fieldMap = new HashMap<String, Class>();
		 eventMap = new HashMap<String, ArrayList<String>>();
		 channelMap = new HashMap<String, ArrayList<String>>();
		 cpu = 0;
	}
	
	@Override
	public int initializeLibrary() {
		return 0;
	}

	@Override
	public JniTracefile allocateNewJniTracefile(
			Jni_C_Pointer_And_Library_Id newPtr, JniTrace newParentTrace)
			throws JniException {
		return null;
	}
	
	public JniEvent readNextEvent() {
		if (events == null || pos >= events.size())
			return null;

		String eventName;
		String traceFileName;
		Element e = events.get(pos++);
		traceFileName = e.getAttributeValue("channel");
		((StubJniTracefile)event.getParentTracefile()).setTracefileName(traceFileName);
		eventName = e.getAttributeValue("name");
		event.setName(eventName);
		Long cpu = Long.parseLong(e.getAttributeValue("cpu"));
		((StubJniTracefile)event.getParentTracefile()).setCpuNumber(cpu);
		Long ts = Long.parseLong(e.getAttributeValue("ts"));
		event.setEventTime(ts);
		
		for(Attribute a: (List<Attribute>) e.getAttributes()) {
			if(a.getName().equals("channel")) {
			} else if (a.getName().equals("name")) {
			} else if (a.getName().equals("cpu")){
			} else if (a.getName().equals("ts")){
			} else {
				
				event.setFieldValue(a.getName(), a.getValue());
			}
		}
		return event;
	}
	
	public void setEventsSource(Document doc) {
		Element root = doc.getRootElement();
		cpu = Integer.parseInt(root.getAttributeValue("cpus"));
		List<Element> channels = root.getChildren("channel");
		String channelName;
		String eventName;
		String fieldName;
		Class fieldType;
		for (Element channel: channels) {
			channelName = channel.getAttributeValue("name");
			if (channelMap.get(channelName) == null) {
				channelMap.put(channelName, new ArrayList<String>());
			}
			ArrayList<String> eventList = channelMap.get(channelName);
			for (Element event: (List<Element>)channel.getChildren("event")) {
				eventName = event.getAttributeValue("name");
				eventList.add(eventName);
				if (eventMap.get(eventName) == null) {
					eventMap.put(eventName, new ArrayList<String>());
				}
				for (Element field: (List<Element>)event.getChildren("field")) {
					fieldName = field.getAttributeValue("name");
					try {
						fieldType = Class.forName("java.lang." + field.getAttributeValue("type"));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						fieldType = Object.class;
					}
					if (fieldMap.get(fieldName) == null) {
						fieldMap.put(fieldName, fieldType);
					}
				}
			}
		}
		events = root.getChild("events").getChildren("event");
	}
	
	@Override
	public int getCpuNumber() {
		return cpu;
	}
}