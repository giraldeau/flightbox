package org.lttng.flightbox.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerState extends TraceEventHandlerBase {

	/* typing entropy: what can we do to better encapsulate? */
	Map<String, StackMachine> machines;
	Map<String, VersionizedStack<String>> objectStates;
	
	public TraceEventHandlerState() {
		super();
		hooks.add(new TraceHook());
		machines = new HashMap<String, StackMachine>();
	}

	public void handleInit(TraceReader reader, JniTrace trace) {
		objectStates = new HashMap<String, VersionizedStack<String>>();
		for (String machineName: machines.keySet()) {
			objectStates.put(machineName, new VersionizedStack<String>());
		}
	}
	
	public void handle_all_event(TraceReader reader, JniEvent event) {
		
		String eventName = event.getMarkersMap().get(event.getEventMarkerId()).getName();
		Long eventTs = event.getEventTime().getTime();
		
		for (StackMachine machine: machines.values()) {
			VersionizedStack stack = objectStates.get(machine.getName());
			machine.step(stack, eventName, eventTs);
		}
	}
	
	public void handleComplete(TraceReader reader) {

	}
	
	public Map<String, VersionizedStack<String>> getObjectState() {
		return objectStates;
	}
	
	public void addStackMachine(StackMachine machine) {
		machines.put(machine.getName(), machine);
	}

	public void addAllStackMachine(Map<String, StackMachine> machinesMap) {
		machines.putAll(machinesMap);
	}
}
