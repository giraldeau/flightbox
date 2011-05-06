package org.lttng.flightbox.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.lttng.flightbox.model.FileDescriptor;

public class BlockingStats {

	private HashMap<Integer, BlockingStatsElement<Integer>> syscallStats;
	private HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats;

	public BlockingStats() {
		reset();
	}

	private void reset() {
		syscallStats = new HashMap<Integer, BlockingStatsElement<Integer>>();
		fdStats = new HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>>();
	}

	public void increment(int syscallId, long delay) {
		if (!syscallStats.containsKey(syscallId)) {
			syscallStats.put(syscallId, new BlockingStatsElement<Integer>(syscallId));
		}
		SummaryStatistics stats = syscallStats.get(syscallId).getSummary();
		stats.addValue(delay);
	}

	public void increment(FileDescriptor fd, long delay) {
		if (!fdStats.containsKey(fd)) {
			fdStats.put(fd, new BlockingStatsElement<FileDescriptor>(fd));
		}
		SummaryStatistics stats = fdStats.get(fd).getSummary();
		stats.addValue(delay);
	}
	
	public HashMap<Integer, BlockingStatsElement<Integer>> getSyscallStats() {
		return syscallStats;
	}
	
	public boolean isEmpty() {
		return syscallStats.isEmpty();
	}
	
	public List<BlockingStatsElement<Integer>> getBlockingStats() {
	    return new ArrayList<BlockingStatsElement<Integer>>(syscallStats.values());
	}

	public void computeStats(Set<BlockingItem> items) {
		reset();
		for (BlockingItem item: items) {
			if (item.getStartTime() == 0)
				continue;
			increment(item.getWaitingSyscall().getSyscallId(), item.getDuration());
			FileDescriptor fd = item.getWaitingSyscall().getFileDescriptor();
			if (fd != null) {
				increment(fd, item.getDuration());
			}
		}
	}

	public HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> getFileDescriptorStats() {
		return this.fdStats;
	}
}
