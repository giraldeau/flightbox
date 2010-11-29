package org.lttng.flightbox.junit;

import org.eclipse.linuxtools.lttng.event.LttngEventContent;

public class LttngEventStub {

	int cpu;
	long ts; 
	LttngEventContent content;
	
	public LttngEventStub() {
		this(0, 0, null);
	}
	
	public LttngEventStub(int cpu, long ts, LttngEventContent content) {
		setCpu(cpu);
		setTs(ts);
		setContent(content);
	}
	
	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public LttngEventContent getContent() {
		return content;
	}

	public void setContent(LttngEventContent content) {
		this.content = content;
	}
	
}
