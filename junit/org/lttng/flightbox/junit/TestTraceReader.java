package org.lttng.flightbox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.junit.Test;
import org.lttng.flightbox.TimeStats;
import org.lttng.flightbox.TimeStatsBucket;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.cpu.TraceEventHandlerStats;
import org.lttng.flightbox.io.TimeKeeper;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceEventHandlerCounter;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.model.Task.TaskState;

public class TestTraceReader {
	
	@Test
	public void testLoadJiniTrace() throws JniException {
		JniTrace trace = JniTraceFactory.getJniTrace(new File(Path.getTraceDir(), "sleep-1x-1sec").toString());
		assertEquals(2, trace.getLttMajorVersion());
		assertEquals(6, trace.getLttMinorVersion());
	}
	
	@Test
	public void testReadAllEvents() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "sleep-1x-1sec").toString();
		JniTrace trace = JniTraceFactory.getJniTrace(trace_path);
		TraceReader reader = new TraceReader(trace_path);
		
		JniEvent e;
		int nbEvents = 0;
		while((e=trace.readNextEvent()) != null){
			nbEvents++;
		}

		TraceEventHandlerCounter eventCountHandler = new TraceEventHandlerCounter();
		reader.register(eventCountHandler);
		reader.process();
		assertEquals(nbEvents, eventCountHandler.getCount());
	}	
	
	@Test
	public void testCpuTraceHandler() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "burn-8x-1sec").toString();
		TraceEventHandlerStats cpu_handler = new TraceEventHandlerStats();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(cpu_handler);
		reader.process();
		TimeStatsBucket total = cpu_handler.getUsageStats().getTotalAvg();
		TimeStats sum = total.getSum();
		assertEquals(1 * TimeStats.NANO, sum.getTime(TaskState.USER), 0.5 * TimeStats.NANO);
	}
	
	@Test
	public void testProcTraceHandler() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "burn-8x-1sec").toString();
		TraceEventHandlerProcess handler = new TraceEventHandlerProcess();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(handler);
		reader.process();
		UsageStats<Long> stats = handler.getUsageStats();
		double enlaps = stats.getDuration();
		for(Long id: stats.idSet()){
			TimeStatsBucket t = stats.getStats(id);
			if(id != 0) {
				assertTrue(t.getSum().getTotal() < enlaps);
			}
		}
		assertEquals(stats.getNumEntry(), handler.getProcInfo().size());
	}

	public class TraceAssertTime extends TraceEventHandlerBase {
		public TraceAssertTime() {
			super();
			hooks.add(new TraceHook());
		}
		public void handle_all_event(TraceReader reader, JniEvent event) {
			TimeKeeper instance = TimeKeeper.getInstance();
			assertEquals(event.getEventTime().getTime(), instance.getCurrentTime());
		}
	}
	
	@Test
	public void testTimeKeeper() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "burn-8x-1sec").toString();
		TraceAssertTime handler = new TraceAssertTime();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(handler);
		reader.process();
	}
}
