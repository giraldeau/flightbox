package org.lttng.flightbox.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerInventory extends TraceEventHandlerBase {

	private MarkerInventoryJDOM inventory;
	
	public TraceEventHandlerInventory() {
		super();
		hooks.add(new TraceHook("metadata", "core_marker_format"));
		inventory = new MarkerInventoryJDOM();
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
	}
		
	public void handle_metadata_core_marker_format(TraceReader reader, JniEvent event) throws JDOMException {
		String channelName = (String) event.parseFieldByName("channel");
		String eventName = (String) event.parseFieldByName("name");
		String payloadFormat = (String) event.parseFieldByName("format");
		String[] fmt = payloadFormat.split(" ");
		if ((fmt.length % 2) != 0) {
			System.out.println("ERROR: " + channelName + "." + eventName + ":" + payloadFormat + " can't split key/values");
			return;
		}
		for (int i=0; i<fmt.length; i+=2) {
			String fieldName = fmt[i];
			String format = fmt[i+1];
			if (format.compareTo("%s") == 0)
				inventory.getOrAddField(channelName, eventName, fieldName, "String");
			else
				inventory.getOrAddField(channelName, eventName, fieldName, "Long");
		}
	}
	
	@Override
	public void handleComplete(TraceReader reader) {
	}

	public Document getInventory() {
		return inventory.getInventory();
	}
}
