package org.lttng.flightbox.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.junit.Test;
import org.lttng.flightbox.cpu.TraceEventHandlerProcess;
import org.lttng.flightbox.cpu.TraceEventHandlerStats;
import org.lttng.flightbox.io.TimeKeeper;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceEventHandlerCounter;
import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.Bucket;
import org.lttng.flightbox.statistics.BucketSeries;
import org.lttng.flightbox.statistics.ResourceUsage;

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
		BucketSeries total = cpu_handler.getUsageStats().getTotalAvg();
		Bucket sum = total.getSum();
		assertEquals(1 * Bucket.NANO, sum.getTime(TaskState.USER), 0.5 * Bucket.NANO);
	}

	@Test
	public void testProcTraceHandler() throws JniException {
		String trace_path = new File(Path.getTraceDir(), "burn-8x-1sec").toString();
		TraceEventHandlerProcess handler = new TraceEventHandlerProcess();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(handler);
		reader.process();
		ResourceUsage<Long> stats = handler.getUsageStats();
		double enlaps = stats.getDuration();
		for(Long id: stats.idSet()){
			BucketSeries t = stats.getStats(id);
			if(id != 0) {
				assertTrue(t.getSum().getTotal() < enlaps);
			}
		}
		assertEquals(stats.getNumEntry(), handler.getProcInfo().size());
	}

	@Test
	public void testCancelReader() throws JniException {
		/*
		 * Compute process list from metadata by reading all the trace
		 * then compare this list with one obtained by reading only metadata
		 */
		String trace_path = new File(Path.getTraceDir(), "burn-8x-1sec").toString();

		// count all events
		TraceReader reader = new TraceReader(trace_path);
		TraceEventHandlerCounter counterHandler = new TraceEventHandlerCounter();
		reader.register(counterHandler);
		reader.process();

		// process only metadata
		SystemModel model1 = new SystemModel();
		TraceEventHandlerModelMeta metaHandler1 = new TraceEventHandlerModelMeta();
		metaHandler1.setModel(model1);
		TraceEventHandlerCounter counterHandler1 = new TraceEventHandlerCounter();
		TraceReader reader1 = new TraceReader(trace_path);
		reader1.register(metaHandler1);
		reader1.register(counterHandler1);
		reader1.process();

		// process all trace
		SystemModel model2 = new SystemModel();
		TraceEventHandlerModelMeta metaHandler2 = new TraceEventHandlerModelMeta();
		metaHandler2.setModel(model2);
		// remove the global_state hook
		Set<TraceHook> hooks = metaHandler2.getHooks();
		TraceHook globalStateHook = null;
		for (TraceHook hook: hooks) {
			if (hook.channelName.equals("global_state")) {
				globalStateHook = hook;
				break;
			}
		}
		hooks.remove(globalStateHook);
		metaHandler2.setHooks(hooks);
		TraceReader reader2 = new TraceReader(trace_path);
		TraceEventHandlerCounter counterHandler2 = new TraceEventHandlerCounter();
		reader2.register(counterHandler2);
		reader2.register(metaHandler2);
		reader2.process();

		// verify that the trace is partially read with end of statedump
		assertTrue(counterHandler1.getCount() < counterHandler2.getCount());
		// verify the trace is read completely without end of statedump
		assertEquals(counterHandler.getCount(), counterHandler2.getCount());

		// verify that the process list are the same
		Set<Integer> pids1 = model1.getTasks().keySet();
		Set<Integer> pids2 = model2.getTasks().keySet();
		assertEquals(pids2, pids1);
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
