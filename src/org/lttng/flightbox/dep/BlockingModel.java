package org.lttng.flightbox.dep;

import java.util.HashMap;
import java.util.TreeSet;

import org.lttng.flightbox.model.Task;

public class BlockingModel {

	private final HashMap<Task, TreeSet<BlockingItem>> blockingItems;
	private final HashMap<Task, BlockingStats> blockingStats;
	
	public BlockingModel() {
		blockingItems = new HashMap<Task, TreeSet<BlockingItem>>();
		blockingStats = new HashMap<Task, BlockingStats>();
	}
	
	public TreeSet<BlockingItem> getBlockingItemsForTask(Task task) {
		TreeSet<BlockingItem> set = blockingItems.get(task);
		if (set == null) {
			set = new TreeSet<BlockingItem>();
			blockingItems.put(task, set);
		}
		return set;
	}

	public BlockingStats getBlockingStatsForTask(Task task) {
		BlockingStats stats = blockingStats.get(task);
		if (stats == null) {
			stats = new BlockingStats();
			TreeSet<BlockingItem> items = getBlockingItemsForTask(task);
			stats.computeStats(items);
			blockingStats.put(task, stats);
		}
		return stats;
	}
	
}
