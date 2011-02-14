package org.lttng.flightbox.junit.stub;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.junit.Test;
import org.lttng.flightbox.stub.StubJniEvent;
import org.lttng.flightbox.stub.StubJniTrace;
import org.lttng.flightbox.stub.StubJniTracefile;
import org.lttng.flightbox.stub.StubTraceReader;

public class TestStubs {

	@Test
	public void testStubJniTracefile() {
		String file = System.getenv("project_loc") + "/tests/stub/process_fork_exit.xml";
		JniTrace trace = StubTraceReader.getJniTrace(file);
		assertEquals(trace.getCpuNumber(), 2);
	}
	
	@Test
	public void testStubJniEvent1() {
		StubJniTracefile traceFile = new StubJniTracefile();
		traceFile.setTracefileName("kernel");
		StubJniEvent event = new StubJniEvent();
		event.setParentTracefile(traceFile);
		String traceFile2 = event.getParentTracefile().getTracefileName();
		assertEquals("kernel", traceFile2);
	}
	
	@Test
	public void testStubJniEvent2() {
		String file = System.getenv("project_loc") + "/tests/stub/process_fork_exit.xml";
		JniTrace trace = StubTraceReader.getJniTrace(file);
		JniEvent ev, ev1, ev2;
		int eventId;
		ArrayList<StubJniEvent> events = new ArrayList<StubJniEvent>();
		ev = trace.readNextEvent();
		JniTime time = new JniTime();
		time.setTime(1);
		assertEquals(ev.getEventTime(), time);
		long cpu = ev.getParentTracefile().getCpuNumber();
		assertEquals(cpu, 0L);
		String eventName = ev.getMarkersMap().get(ev.getEventMarkerId()).getName();
		assertEquals(eventName, "process_fork");
		assertEquals(ev.parseFieldByName("child_pid"), "21207");
		
		/*
		while((ev=trace.readNextEvent()) != null) {
			System.out.println(ev.getEventMarkerId());
			System.out.println(ev.getMarkersMap().get(ev.getEventMarkerId()).getName());
		}*/
	}
}
