package org.lttng.flightbox.junit;

import org.junit.Test;

public class TestThread {

	@Test
	public void testThreadException() {
		Thread t = new Thread() {
			public void run() {
				throw new RuntimeException();
			}
		};
	}
}
