package org.lttng.flightbox.dep;

import java.util.HashMap;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class BlockingStats {

	private final HashMap<Integer, SummaryStatistics> syscallStats;

	public BlockingStats() {
		syscallStats = new HashMap<Integer, SummaryStatistics>();
	}

	public void increment(int syscallId, long delay) {
		if (!syscallStats.containsKey(syscallId)) {
			syscallStats.put(syscallId, new SummaryStatistics());
		}
		SummaryStatistics stats = syscallStats.get(syscallId);
		stats.addValue(delay);
	}

	public HashMap<Integer, SummaryStatistics> getSyscallStats() {
		return syscallStats;
	}
	
	public boolean isEmpty() {
		return syscallStats.isEmpty();
	}

}
