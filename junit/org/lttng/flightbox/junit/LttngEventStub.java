package org.lttng.flightbox.junit;

public class LttngEventStub {

	int cpu;
	long ts; 
	//LttngEventContent content;
	Object content;
	
	public LttngEventStub() {
		this(0, 0, null);
	}
	
	public LttngEventStub(int cpu, long ts, Object content) {
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

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}
	
}
