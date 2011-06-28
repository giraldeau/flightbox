package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lttng.flightbox.statistics2.Samples;

public class TestSamples {

	@Test
	public void testSimpleSamples() {
		Samples s = new Samples(10, 110, 10);
		assertEquals(0, s.sampleIndex(10));
		assertEquals(0, s.sampleIndex(11));
		assertEquals(0, s.sampleIndex(19));
		assertEquals(1, s.sampleIndex(20));
		assertEquals(1, s.sampleIndex(21));
		assertEquals(9, s.sampleIndex(110));
		
		Exception exp = null;
		try {
			s.sampleIndex(111);
		} catch (Exception e) {
			exp = e;
		}
		assertNotNull(exp);
	
		exp = null;
		try {
			s.sampleIndex(9);
		} catch (Exception e) {
			exp = e;
		}
		assertNotNull(exp);
	}
	
}
