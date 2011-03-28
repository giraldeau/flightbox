package org.lttng.flightbox.histogram;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

import statehistory.StateHistorySystem;
import statehistory.common.AttributeNotFoundException;

public class TraceEventHandlerHistogramSHT extends TraceEventHandlerBase implements IHistogramHandler {

	private int nbSamples = 100;
	private int[] samples;
	private long start;
	private long end;
	private long duration;
	private int factor;
	private StateHistorySystem shs;

	public TraceEventHandlerHistogramSHT() {
		super();
		hooks.add(new TraceHook());
	}

	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		samples = new int[nbSamples];
		start = trace.getStartTime().getTime();
		end = trace.getEndTime().getTime();
		duration = end - start;
		factor = (int) (nbSamples / duration);
		// FIXME: never allocate a relative file inside handler
		// this is a coding horror
		File temp = null;
		try {
			temp = File.createTempFile("history",".shs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (temp == null)
			return;
		try {
			shs = new StateHistorySystem(temp.getPath(), start, 4096 * 4, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		shs.modifyAttribute(start, 0, "stats", "histogram");
	}

	@Override
	public void handleComplete(TraceReader reader) {

	}

	public void handle_all_event(TraceReader reader, JniEvent event) {
		if (shs == null)
			return;
		long t = event.getEventTime().getTime();
		int stateValueInt = 0;
		try {
			stateValueInt = shs.getCurrentStateValueInt("stats", "histogram");
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
			return;
		}
		stateValueInt++;
		shs.modifyAttribute(t, stateValueInt, "stats", "histogram");

		//shs.incrementAttribute(t, "stats", "histogram");
	}

	public int[] getSamples() throws AttributeNotFoundException {
		samples = new int[nbSamples];
		int currStateValueInt = 0;
		int prevStateValueInt = 0;
		long tCurr = 0;
		int delta = 0;
		long offset = 0;
		for(int i = 0; i< nbSamples; i++) {
			offset = ((i + 1) * duration) / nbSamples;
			tCurr = offset + start;
			currStateValueInt = shs.getSingleStateValueInt(tCurr, "stats", "histogram");
			delta = currStateValueInt - prevStateValueInt;
			samples[i] = delta;
			prevStateValueInt = currStateValueInt;
		}
		return samples;
	}

	public void setNbSamples(int s) {
		nbSamples = s;
	}

}
