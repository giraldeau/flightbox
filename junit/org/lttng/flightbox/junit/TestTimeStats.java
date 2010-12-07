package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.TimeStats;

public class TestTimeStats {
	
	static double p = 0.0001;
	
	@Test
	public void testCreateTimeStats() {
		TimeStats t = new TimeStats();
		assertEquals(0,t.getTotal(), p);
	}
	
	@Test
	public void testStatsLogic() {
		TimeStats t = new TimeStats();
		t.addTime(1.0, KernelMode.IRQ);
		t.addTime(2.0, KernelMode.SYSCALL);
		t.addTime(3.0, KernelMode.TRAP);
		t.addTime(4.0, KernelMode.USER);
		assertEquals(10, t.getTotal(), p);
		assertEquals(4, t.getTime(KernelMode.USER), p);
		assertEquals(6, t.getSystem(), p);
	}
	
	@Test
	public void testInterval() {
		TimeStats t = new TimeStats();
		t.setStartTime(1.2);
		t.setEndTime(1.6);
		assertEquals(0.4, t.getDuration(), p);
		assertEquals(0.4, t.getIdle(), p);
	}
	
	@Test
	public void testAverages() {
		TimeStats t = new TimeStats();
		t.setStartTime(10.0);
		t.setEndTime(30.0);
		t.addTime(1.0, KernelMode.IRQ);
		t.addTime(2.0, KernelMode.SYSCALL);
		t.addTime(3.0, KernelMode.TRAP);
		t.addTime(4.0, KernelMode.USER);
		assertEquals(0.5, t.getTotalAvg(), p);
		assertEquals(0.2, t.getAvg(KernelMode.USER), p);
		assertEquals(0.3, t.getSystemAvg(), p);
		assertEquals(0.15, t.getAvg(KernelMode.TRAP), p);
		assertEquals(0.05, t.getAvg(KernelMode.IRQ), p);
		assertEquals(0.1, t.getAvg(KernelMode.SYSCALL), p);
		assertEquals(0.5, t.getIdleAvg(), p);
	}
	
	@Test
	public void testAddTimeStats() {
		TimeStats t1 = new TimeStats();
		t1.setStartTime(10.0);
		t1.setEndTime(30.0);
		t1.addTime(1.0, KernelMode.IRQ);
		t1.addTime(2.0, KernelMode.SYSCALL);
		t1.addTime(3.0, KernelMode.TRAP);
		t1.addTime(4.0, KernelMode.USER);
		TimeStats t2 = new TimeStats();
		t2.setStartTime(5.0);
		t2.setEndTime(8.0);
		t2.addTime(1.0, KernelMode.IRQ);
		t2.addTime(2.0, KernelMode.SYSCALL);
		t2.addTime(3.0, KernelMode.TRAP);
		t2.addTime(4.0, KernelMode.USER);
		t2.add(t1);
		assertEquals(20, t2.getTotal(), p);
		assertEquals(5, t2.getStartTime(), p);
		assertEquals(30, t2.getEndTime(), p);
	}
	
	@Test
	public void testGenericMethods() {
		TimeStats t1 = new TimeStats();
		t1.addTime(1.0, KernelMode.USER);
		assertEquals(1.0, t1.getTime(KernelMode.USER), 0);
	}
	
	@Test
	public void testAddInterval() {
		KernelMode mode = KernelMode.USER;
		TimeStats t1 = new TimeStats();
		t1.setStartTime(10.0);
		t1.setEndTime(30.0);
		// Interval outside range
		t1.addInterval(1, 5, mode);
		t1.addInterval(35, 40, mode);
		assertEquals(0, t1.getTime(mode), p);
		t1.clear();
		// Interval inside one interval
		t1.addInterval(11, 12, mode);
		t1.addInterval(13, 14, mode);
		assertEquals(2, t1.getTime(mode), p);
		t1.clear();
		// Interval starts before
		t1.addInterval(5, 15, mode);
		assertEquals(5, t1.getTime(mode), p);
		t1.clear();
		// Interval starts after
		t1.addInterval(25, 35, mode);
		assertEquals(5, t1.getTime(mode), p);
		t1.clear();
		// Interval starts before and end after
		t1.addInterval(5, 35, mode);
		assertEquals(20, t1.getTime(mode), p);
		t1.clear();
	}
}
