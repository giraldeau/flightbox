package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lttng.flightbox.model.KernelTask;

public class TestKernelTask {

	@Test
	public void testCompareTasks() {
		KernelTask t1 = new KernelTask(1, 10L);
		KernelTask t2 = new KernelTask(1, 10L);
		KernelTask t3 = new KernelTask(2, 10L);
		KernelTask t4 = new KernelTask(1, 20L);
		
		assertEquals(0, t1.compareTo(t2));
		assertEquals(-1, t1.compareTo(t3));
		assertEquals(-1, t1.compareTo(t4));
		assertEquals(-1, t4.compareTo(t3));
		assertEquals(1, t3.compareTo(t4));
	}
	
}
