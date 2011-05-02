package org.lttng.flightbox.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class BlockingStats {

	private final HashMap<Integer, BlockingSummaryStatistics> syscallStats;

	public BlockingStats() {
		syscallStats = new HashMap<Integer, BlockingSummaryStatistics>();
	}

	public void increment(int syscallId, long delay) {
		if (!syscallStats.containsKey(syscallId)) {
			syscallStats.put(syscallId, new BlockingSummaryStatistics(syscallId));
		}
		SummaryStatistics stats = syscallStats.get(syscallId).getSummary();
		stats.addValue(delay);
	}

	public HashMap<Integer, BlockingSummaryStatistics> getSyscallStats() {
		return syscallStats;
	}
	
	public boolean isEmpty() {
		return syscallStats.isEmpty();
	}
	
	public List<BlockingSummaryStatistics> getBlockingStats() {
	    return new ArrayList<BlockingSummaryStatistics>(syscallStats.values());
	}

}
