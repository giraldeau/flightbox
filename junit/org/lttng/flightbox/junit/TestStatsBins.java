package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lttng.flightbox.StatsBins;
import org.lttng.flightbox.TimeStats;

public class TestStatsBins {

	static double p = 0.0001;
	
	@Test
	public void testIndexCalculation() {
		assertEquals(0, (int)Math.floor(0.1));
		assertEquals(0, (int)Math.floor(0.5));
		assertEquals(0, (int)Math.floor(0.6));
		assertEquals(1, (int)Math.floor(1.6));
	}
	
	@Test
	public void testCreateStatsBins() {
		StatsBins s = new StatsBins();
		assertEquals(0, s.size());
		assertEquals(0, s.getBinDuration(), p);
		assertEquals(0, s.getStartTime(), p);
		assertEquals(0, s.getEndTime(), p);
	}
	
	@Test
	public void testInitBins() {
		StatsBins s = new StatsBins();
		s.init(0.5, 0.8, 3);
		assertEquals(3, s.size());
		assertEquals(0.1, s.getBinDuration(), p);
		TimeStats t1 = s.getIntervalByTime(0.65);
		assertEquals(0.6, t1.getStartTime(), p);
		assertEquals(0.7, t1.getEndTime(), p);
		t1.addUser(0.1);
		TimeStats t2 = s.getIntervalByTime(0.75);
		t2.addUser(0.2);
		assertEquals(t1.getUser() + t2.getUser(), s.getSum().getUser(), p);
	}
}
