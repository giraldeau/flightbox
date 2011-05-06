package org.lttng.flightbox.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class BlockingStats {

	private HashMap<Integer, BlockingSummaryStatistics> syscallStats;

	public BlockingStats() {
		reset();
	}

	private void reset() {
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

	public void computeStats(Set<BlockingItem> items) {
		reset();
		for (BlockingItem item: items) {
			if (item.getStartTime() > 0) {
				increment(item.getWaitingSyscall().getSyscallId(), item.getDuration());
			}
		}
	}
}
