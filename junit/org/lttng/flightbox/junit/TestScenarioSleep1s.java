package org.lttng.flightbox.junit;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.linuxtools.lttng.event.LttngEventField;

// I hate LttngEvent class: we can't create stub objects easily!

public class TestScenarioSleep1s {

	ArrayList<LttngEventStub> events; 
	ArrayList<LttngClient> clients;
	
	public TestScenarioSleep1s() {
		events = new ArrayList<LttngEventStub>();
		clients = new ArrayList<LttngClient>();
		init();
	}
	
	private void init() {
		HashMap<String, LttngEventField> content;
		content = new HashMap<String, LttngEventField>();
		//content.put(, value)
		//events.add(new LttngEventStub(0, 0, content));
	}

	public void registerClient(LttngClient client) {
		clients.add(client);
	}
	
	public void process() {
		for(LttngClient client: clients) {
			for (LttngEventStub event: events) {
				client.handleData(event);
			}
		}
	}
	
}
