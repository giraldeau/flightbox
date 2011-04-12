package org.lttng.flightbox.dep;

import java.util.HashMap;

public class BlockingStats {

	private final HashMap<Integer, Long> syscallToSum;

	public BlockingStats() {
		syscallToSum = new HashMap<Integer, Long>();
	}

	public void increment(int syscallId, long delay) {
		if (!syscallToSum.containsKey(syscallId)) {
			syscallToSum.put(syscallId, 0L);
		}
		Long total = syscallToSum.get(syscallId);
		total += delay;
		syscallToSum.put(syscallId, total);
	}

	public HashMap<Integer, Long> getSyscallToSum() {
		return syscallToSum;
	}

}
