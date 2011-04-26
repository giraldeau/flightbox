package org.lttng.flightbox.histogram;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

import statehistory.StateHistorySystem;

public class TraceEventHandlerHistogramSHT extends TraceEventHandlerBase {

	public static final String[] ATTRIBUTE_PATH = { "stats", "histogram" };
    private long start, end;
	private StateHistorySystem shs;
	private int attributeId;

	public TraceEventHandlerHistogramSHT() {
		super();
		hooks.add(new TraceHook());
	}

	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		start = trace.getStartTime().getTime();
		end = trace.getEndTime().getTime();
		if (shs == null) {
			throw new RuntimeException("StateHistorySystem is not set");
		}
		attributeId = getAttributeId();
		shs.modifyAttribute(start, 0, attributeId);
	}

	@Override
	public void handleComplete(TraceReader reader) {
	    int max = shs.getCurrentStateValueInt(attributeId);
	    shs.modifyAttribute(end, max, attributeId);
		shs.closeTree();
	}

	public void handle_all_event(TraceReader reader, JniEvent event) {
		if (shs == null)
			return;
		long t = event.getEventTime().getTime();
		shs.incrementAttribute(t, attributeId);
	}

	public void setStateHistorySystem(StateHistorySystem shs) {
		this.shs = shs;
	}

    public int getAttributeId() {
        return shs.getAttributeQuarkAndAdd(ATTRIBUTE_PATH);
    }

}
