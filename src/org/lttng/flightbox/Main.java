package org.lttng.flightbox;

import java.io.File;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.lttng.flightbox.cpu.TraceEventHandlerStats;
import org.lttng.flightbox.io.EventQuery;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.statistics.Bucket;
import org.lttng.flightbox.statistics.BucketSeries;

public class Main {

	/**
	 * @param args
	 * @throws JniException 
	 */
	public static void main(String[] args) throws JniException {
		String trace_dir = System.getenv("TRACE_DIR");
		if (trace_dir == null) {
			throw new RuntimeException("TRACE_DIR not set");
		}
		String trace_path = new File(trace_dir, "burn-8x-1sec").toString();
		EventQuery sched_query = new EventQuery();
		sched_query.addEventType("kernel");
		sched_query.addEventName("sched_schedule");
		TraceEventHandlerStats cpu_handler = new TraceEventHandlerStats();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(cpu_handler);
		reader.process();
		System.out.println(cpu_handler.getUsageStats());
		BucketSeries total = cpu_handler.getUsageStats().getTotal();
		Bucket sum = total.getSum();
		System.out.println(sum);
	}

}
