package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lttng.flightbox.model.Processor;
import org.lttng.flightbox.model.Processor.ProcessorState;
import org.lttng.flightbox.model.ProcessorListener;

public class TestProcessor {

	@Test
	public void testProcessor() {
		Processor p1 = new Processor(0);
		Processor p2 = new Processor(0);
		Processor p3 = new Processor(1);
		assertTrue(p1.equals(p2));
		assertEquals(1, p3.compareTo(p1));
	}

	@Test
	public void testProcessorStateListener() {
		Processor p1 = new Processor();
		class Klass extends ProcessorListener {
			public ProcessorState prev = null;
			public ProcessorState next = null;
			public boolean prevpm = false;
			public boolean nextpm = false;
			@Override
			public void stateChange(Processor processor, ProcessorState nextState) {
				prev = processor.peekState();
				next = nextState;
			}
			@Override
			public void lowPowerModeChange(Processor processor, boolean nextLowPowerMode) {
				prevpm = processor.isLowPowerMode();
				nextpm = nextLowPowerMode;
			}
		};
		Klass listener = new Klass();
		p1.addListener(listener);
		
		p1.pushState(ProcessorState.IRQ);
		assertEquals(null, listener.prev);
		assertEquals(ProcessorState.IRQ, listener.next);
		
		p1.pushState(ProcessorState.SOFTIRQ);
		assertEquals(ProcessorState.IRQ, listener.prev);
		assertEquals(ProcessorState.SOFTIRQ, listener.next);
		
		p1.setLowPowerMode(false);
		assertEquals(false, listener.prevpm);
		assertEquals(false, listener.nextpm);

		p1.setLowPowerMode(true);
		assertEquals(false, listener.prevpm);
		assertEquals(true, listener.nextpm);

		p1.setLowPowerMode(false);
		assertEquals(true, listener.prevpm);
		assertEquals(false, listener.nextpm);
		
		listener.prev = null;
		listener.next = null;
		p1.removeListener(listener);
		p1.pushState(ProcessorState.SYSCALL);
		assertEquals(null, listener.prev);
		assertEquals(null, listener.next);
	}

}
