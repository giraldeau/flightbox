package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.flightbox.CpuUsageStats;
import org.lttng.flightbox.TimeStatsBucket;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.TimeStats;
import org.lttng.flightbox.cpu.TraceEventHandlerCpu;
import org.lttng.flightbox.io.EventQuery;
import org.lttng.flightbox.io.TraceEventHandler;
import org.lttng.flightbox.io.TraceEventHandlerCounter;
import org.lttng.flightbox.io.TraceReader;

public class TestTraceReader {
	
	static String trace_dir;
	static public String trace_dir_var = "TRACE_DIR";
	static public String[] trace_names = {"sleep-1x-1sec", "burn-1x-1sec", "burn-8x-1sec"};
	
	@BeforeClass
	public static void setUp() {
		trace_dir = System.getenv(trace_dir_var);
		if (trace_dir == null) {
			throw new RuntimeException("TRACE_DIR not set");
		}
	}
	
	@Test
	public void testLoadJiniTrace() throws JniException {
		JniTrace trace = JniTraceFactory.getJniTrace(new File(trace_dir, "sleep-1x-1sec").toString());
		assertEquals(2, trace.getLttMajorVersion());
		assertEquals(6, trace.getLttMinorVersion());
	}
	
	@Test
	public void testReadNextEvent() throws JniException {
		String trace_path = new File(trace_dir, "sleep-1x-1sec").toString();
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
	
	//@Test
	public void testMarkerMap() throws JniException {
		String trace_path = new File(trace_dir, "sleep-1x-1sec").toString();
		JniTrace trace = JniTraceFactory.getJniTrace(trace_path);
		
		JniEvent e;
		int nbEvents = 0;
		while((e=trace.readNextEvent()) != null && nbEvents < 10){
			nbEvents++;
			System.out.println("\n\nMARKER MAP");
			System.out.println(e.getMarkersMap());
		}
		
		//trace.printTraceInformation();
		//HashMap<String, JniTracefile> map = trace.getTracefilesMap();
		//for(String s : map.keySet()) {
		//	System.out.println(s);
		//}
		
		/*
		System.out.println(e.getEventMarkerId());
		System.out.println(e.getEventState());
		System.out.println(e.getMarkersMap().get(e.getEventMarkerId()));
		System.out.println(e.parseFieldByName("ip"));
		System.out.println(e.parseFieldByName("syscall_id"));
		System.out.println(e.getEventDataSize());
		JniTracefile tracefile = map.get("task_state_0");
		*/
	}
	
	@Test
	public void testEventQuery() throws JniException {
		String trace_path = new File(trace_dir, "sleep-1x-1sec").toString();
		JniTrace trace = JniTraceFactory.getJniTrace(trace_path);
		
		Long cpu = 0L;
		EventQuery q = new EventQuery();
		q.addCpu(cpu);
		
		
		JniEvent e;
		int nbEvents = 0;
		int matchEvents = 0;
		Boolean isMatch;
		while((e=trace.readNextEvent()) != null){
			nbEvents++;
			isMatch = q.match(e);
			if (isMatch) {
				matchEvents++;
			}
			assertEquals(isMatch.booleanValue(), e.getParentTracefile().getCpuNumber() == cpu);
		}
		assertTrue(nbEvents >= matchEvents);
	}
	
	@Test
	public void testCpuTraceHandler() throws JniException {
		String trace_path = new File(trace_dir, "burn-8x-1sec").toString();
		EventQuery sched_query = new EventQuery();
		sched_query.addEventType("kernel");
		sched_query.addEventName("sched_schedule");
		TraceEventHandlerCpu cpu_handler = new TraceEventHandlerCpu();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(sched_query, cpu_handler);
		reader.process();
		System.out.println(cpu_handler.getCpuUsageStats());
		TimeStatsBucket total = cpu_handler.getCpuUsageStats().getTotal();
		TimeStats sum = total.getSum();
		assertEquals(8 * TimeStats.NANO, sum.getTime(KernelMode.USER), 1 * TimeStats.NANO);
	}
}
