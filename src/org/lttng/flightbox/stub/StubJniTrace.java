package org.lttng.flightbox.stub;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.lttng.flightbox.io.TraceReader;

public class StubJniTrace extends JniTrace {

	private List<Element> events;
	private int pos;
	StubJniEvent event;
	HashMap<String, HashMap<String, HashMap<String, Class>>> channelMap;
	private int cpu;
	
	public StubJniTrace() {
		 event = new StubJniEvent();
		 event.setParentTracefile(new StubJniTracefile());
		 channelMap = new HashMap<String, HashMap<String, HashMap<String, Class>>>();
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
				Object val = null;
				HashMap<String, HashMap<String, Class>> eventMap = channelMap.get(traceFileName);
				if (eventMap == null)
					throw new RuntimeErrorException(null, "Definition for tracefile " + traceFileName + " not found");
				HashMap<String, Class> fieldMap = eventMap.get(eventName);
				if (fieldMap == null)
					throw new RuntimeErrorException(null, "Definition for event " + eventName + " not found");
				Class klass = fieldMap.get(a.getName());
				if (klass == null)
					throw new RuntimeErrorException(null, "Definition for event " + eventName + " not found");
				try {
					val = castString(a.getValue(), klass);
				} catch (Exception exception) {
					
				}
				event.setFieldValue(a.getName(), val);
			}
		}
		return event;
	}
	
	public Object castString(String s, Class type) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class[] argTypes = new Class[] { String.class };
		Constructor cst = type.getConstructor(argTypes);
		if (cst == null) 
			throw new ClassCastException("Unknown constructor " + type.toString() + "(String s)");
		return cst.newInstance(s);
	}
	
	public void setEventsSource(Document doc, Document inventory) {
		Element root = doc.getRootElement();
		cpu = Integer.parseInt(root.getAttributeValue("cpus"));
		
		Element invRoot = inventory.getRootElement();
		List<Element> channels = invRoot.getChildren("channel");
		String channelName;
		String eventName;
		String fieldName;
		Class fieldType;
		for (Element channel: channels) {
			
			/* Add channel */
			channelName = channel.getAttributeValue("name");
			HashMap<String, HashMap<String, Class>> eventMap = channelMap.get(channelName); 
			if (eventMap == null) {
				eventMap = new HashMap<String, HashMap<String, Class>>();
				channelMap.put(channelName, eventMap);
			}

			for (Element event: (List<Element>)channel.getChildren("event")) {
				
				/* Add event */
				eventName = event.getAttributeValue("name");
				HashMap<String, Class> fieldMap = eventMap.get(eventName);
				if (fieldMap == null) {
					fieldMap = new HashMap<String, Class>();
					eventMap.put(eventName, fieldMap);
				}
				
				for (Element field: (List<Element>)event.getChildren("field")) {
					/* Add field */
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

	public void setInventory(Document inventory) {
		
	}
	
	@Override
	public JniTime getStartTime() {
		return new JniTime();
	}
}