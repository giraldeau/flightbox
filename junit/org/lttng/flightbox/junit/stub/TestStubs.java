package org.lttng.flightbox.junit.stub;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.TraceEventHandlerCounter;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.stub.StubJniEvent;
import org.lttng.flightbox.stub.StubJniTrace;
import org.lttng.flightbox.stub.StubJniTracefile;
import org.lttng.flightbox.stub.StubTraceReader;

public class TestStubs {

	@Test
	public void testStubJniTracefile() {
		File file = new File(Path.getTestStubDir(), "process_fork_exit.xml");
		JniTrace trace = StubTraceReader.getJniTrace(file.getPath());
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
		File file = new File(Path.getTestStubDir(), "process_fork_exit.xml");
		JniTrace trace = StubTraceReader.getJniTrace(file.getPath());
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
		assertEquals(eventName, "process_state");
		assertEquals(((Long)ev.parseFieldByName("pid")).longValue(), 0L);
		
	}
	
	@Test
	public void testStubCountEvents() throws JniException {
		File file = new File(Path.getTestStubDir(), "process_fork_exit.xml");
		StubTraceReader reader = new StubTraceReader(file.getPath());
		TraceEventHandlerCounter handler = new TraceEventHandlerCounter();
		reader.register(handler);
		reader.process();
		assertEquals(9, handler.getCount());
	}
	
	@Test
	public void testCasts() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		StubJniTrace trace = new StubJniTrace();
		Object obj = null;

		obj = trace.castString("1", Integer.class);
		Integer expInteger = new Integer("1");
		assertEquals(expInteger, obj);
		
		obj = trace.castString("1", Long.class);
		Long expLong = new Long("1");
		assertEquals(expLong , obj);
		
		obj = trace.castString("1", Double.class);
		Double expDouble = new Double("1");
		assertEquals(expDouble, obj);
		
		obj = trace.castString("1", String.class);
		String expString = new String("1");
		assertEquals(expString, obj);

		Exception e = null;
		try {
			obj = trace.castString("foo", Double.class);
		} catch (Exception x) {
			e = x;
		}
		assertNotNull(e);
	}
	
}
