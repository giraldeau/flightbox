package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import org.junit.Test;
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
		t.addIrq(1.0);
		t.addSyscall(2.0);
		t.addTrap(3.0);
		t.addUser(4.0);
		assertEquals(10, t.getTotal(), p);
		assertEquals(4, t.getUser(), p);
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
		t.addIrq(1.0);
		t.addSyscall(2.0);
		t.addTrap(3.0);
		t.addUser(4.0);
		assertEquals(0.5, t.getTotalAvg(), p);
		assertEquals(0.2, t.getUserAvg(), p);
		assertEquals(0.3, t.getSystemAvg(), p);
		assertEquals(0.15, t.getTrapAvg(), p);
		assertEquals(0.05, t.getIrqAvg(), p);
		assertEquals(0.1, t.getSyscallAvg(), p);
		assertEquals(0.5, t.getIdleAvg(), p);
	}
	
	@Test
	public void testAddTimeStats() {
		TimeStats t1 = new TimeStats();
		t1.setStartTime(10.0);
		t1.setEndTime(30.0);
		t1.addIrq(1.0);
		t1.addSyscall(2.0);
		t1.addTrap(3.0);
		t1.addUser(4.0);
		TimeStats t2 = new TimeStats();
		t2.setStartTime(5.0);
		t2.setEndTime(8.0);
		t2.addIrq(1.0);
		t2.addSyscall(2.0);
		t2.addTrap(3.0);
		t2.addUser(4.0);
		t2.add(t1);
		assertEquals(20, t2.getTotal(), p);
		assertEquals(5, t2.getStartTime(), p);
		assertEquals(30, t2.getEndTime(), p);
	}
}
