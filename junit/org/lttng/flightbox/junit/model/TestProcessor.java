package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lttng.flightbox.model.Processor;

public class TestProcessor {

	@Test
	public void testProcessor() {
		Processor p1 = new Processor(0);
		Processor p2 = new Processor(0);
		Processor p3 = new Processor(1);
		assertTrue(p1.equals(p2));
		assertEquals(1, p3.compareTo(p1));
	}
	
}
