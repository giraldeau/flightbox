package org.lttng.flightbox.dep;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.TaskListener;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.WaitInfo;

public class BlockingTaskListener extends TaskListener {

	private final HashMap<Task, TreeSet<BlockingTree>> blockingItems;
	private final HashMap<Task, BlockingStats> blockingStats;

	public BlockingTaskListener() {
		blockingItems = new HashMap<Task, TreeSet<BlockingTree>>();
		blockingStats = new HashMap<Task, BlockingStats>();
	}

	@Override
	public void popState(Task task, StateInfo nextState) {

		StateInfo info = task.peekState();
		if (info.getTaskState() != TaskState.WAIT)
			return;

		WaitInfo wait = (WaitInfo) info;
		if (!wait.isBlocking())
			return;

		BlockingTree item = new BlockingTree();
		/* copy relevant data */
		item.setStartTime(wait.getStartTime());
		item.setEndTime(wait.getEndTime());
		item.setWaitingSyscall(wait.getWaitingSyscall());
		item.setWakeUp(wait.getWakeUp());
		item.setWakeUpTask(wait.getWakeUpTask());
		item.setTask(wait.getTask());

		/* add this blocking item to the process */
		TreeSet<BlockingTree> set = blockingItems.get(task);
		if (set == null) {
			set = new TreeSet<BlockingTree>();
			blockingItems.put(task, set);
		}
		set.add(item);

		/* increment statistics */
		BlockingStats stats = getBlockingStats().get(task);
		if (stats == null) {
			stats = new BlockingStats();
			blockingStats.put(task, stats);
		}
		// if time is zero, assume we don't know it and avoid increment
		if (wait.getStartTime() > 0) {
			stats.increment(wait.getWaitingSyscall().getSyscallId(), wait.getDuration());
		}

	}

	public SortedSet<BlockingTree> getBlockingItemsForTask(Task task) {
		SortedSet<BlockingTree> result = null;
		if (blockingItems.containsKey(task))
			result = blockingItems.get(task);
		return result;
	}

	public HashMap<Task, BlockingStats> getBlockingStats() {
		return blockingStats;
	}

}
