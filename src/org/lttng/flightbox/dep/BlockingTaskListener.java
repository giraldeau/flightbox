package org.lttng.flightbox.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lttng.flightbox.model.StateInfo;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.TaskListener;
import org.lttng.flightbox.model.WaitInfo;

public class BlockingTaskListener extends TaskListener {

	private final HashMap<Task, List<BlockingItem>> blockingItems;
	private final List<BlockingItem> allBlockingItems;

	public BlockingTaskListener() {
		blockingItems = new HashMap<Task, List<BlockingItem>>();
		allBlockingItems = new ArrayList<BlockingItem>();
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
		StateInfo info = task.peekState();
		if (info instanceof WaitInfo) {
			WaitInfo wait = (WaitInfo) info;
			if (wait.isBlocking()) {
				BlockingItem item = new BlockingItem();
				item.setWaitInfo(wait);
				item.setTask(task);
				List<BlockingItem> list = blockingItems.get(task);
				if (list == null) {
					list = new ArrayList<BlockingItem>();
					blockingItems.put(task, list);
				}
				list.add(item);
				allBlockingItems.add(item);
			}
		}
	}

	public List<BlockingItem> getAllBlockingItems() {
		return allBlockingItems;
	}

	public List<BlockingItem> getBlockingItemsForTask(Task task) {
		return blockingItems.get(task);
	}

}
