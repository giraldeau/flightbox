package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.GlobalState.KernelMode;

public class TestCpuUsageStats {

	public static double p = 0.0001; 
	
	@Test
	public void testAddInterval() {
		UsageStats<Long> stats = new UsageStats<Long>(0L, 30L, 100);
		KernelMode mode = KernelMode.USER;
		stats.addInterval(10L, 20L, 0L, mode);
		stats.addInterval(15L, 16L, 1L, mode);
		// total for all items
		assertEquals(11, stats.getTotal().getSum().getTime(mode), p);
		// total for one item
		assertEquals(10, stats.getStats(0L).getSum().getTime(mode), p);
		assertEquals(1, stats.getStats(1L).getSum().getTime(mode), p);
		// total for one interval
		double t0 = stats.getTotal().getIntervalByTime(5).getTime(mode);
		double t1 = stats.getTotal().getIntervalByTime(11).getTime(mode);
		double t2 = stats.getTotal().getIntervalByTime(15.5).getTime(mode);
		double t3 = stats.getTotal().getIntervalByTime(17).getTime(mode);
		assertEquals(0, t0, p);
		assertEquals(t1 * 2, t2, p);
		assertEquals(t1, t3, p);
		// total for one item for one interval
		assertEquals(0.3, stats.getStats(0L).getIntervalByTime(15).getTime(mode), p);
	}
	
	@Test
	public void testAddIntervalAvg() {
		UsageStats<Long> stats = new UsageStats<Long>(0L, 30L, 100);
		KernelMode mode = KernelMode.USER;
		stats.addInterval(10L, 20L, 0L, mode);
		stats.addInterval(15L, 16L, 1L, mode);
		// total for all items
		assertEquals(11.0 / (double)stats.getNumEntry(), stats.getTotalAvg().getSum().getTime(mode), p);
		// total for one item
		assertEquals(10, stats.getStats(0L).getSum().getTime(mode), p);
		assertEquals(1, stats.getStats(1L).getSum().getTime(mode), p);
		// total for one interval
		double t0 = stats.getTotalAvg().getIntervalByTime(5).getTime(mode);
		double t1 = stats.getTotalAvg().getIntervalByTime(11).getTime(mode);
		double t2 = stats.getTotalAvg().getIntervalByTime(15.5).getTime(mode);
		double t3 = stats.getTotalAvg().getIntervalByTime(17).getTime(mode);
		assertEquals(0, t0, p);
		assertEquals(t1 * 2, t2, p);
		assertEquals(t1, t3, p);
		// total for one item for one interval
		assertEquals(0.3, stats.getStats(0L).getIntervalByTime(15).getTime(mode), p);
	}
}
