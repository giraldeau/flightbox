package org.lttng.flightbox.histogram;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;

public class TraceEventHandlerHistogram extends TraceEventHandlerBase {

	private int nbSamples = 100;
	private int[] samples;
	private long start;
	private long end;
	private long duration;
	private int factor;
	
	public TraceEventHandlerHistogram() {
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
	}

	@Override
	public void handleComplete(TraceReader reader) {
		
	}
	
	public void handle_all_event(TraceReader reader, JniEvent event) {
		long t = event.getEventTime().getTime();
		int x = (int) ((t - start) * nbSamples / duration);
		samples[x]++;
	}
	
	public int[] getSamples() {
		return samples;
	}

	public void setNbSamples(int s) {
		nbSamples = s;
	}

}
