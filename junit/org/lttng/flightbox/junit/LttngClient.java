package org.lttng.flightbox.junit;

public interface LttngClient {

	public void handleData(LttngEventStub event);
	
}
