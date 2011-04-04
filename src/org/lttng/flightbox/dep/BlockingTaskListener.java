package org.lttng.flightbox.dep;

import java.util.ArrayList;
import java.util.List;

import org.lttng.flightbox.model.StateInfo;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.TaskListener;
import org.lttng.flightbox.model.WaitInfo;

public class BlockingTaskListener extends TaskListener {

	private final List<BlockingItem> blockingItems;

	public BlockingTaskListener() {
		blockingItems = new ArrayList<BlockingItem>();
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
				blockingItems.add(item);
			}
		}
	}

	public List<BlockingItem> getBlockingItems() {
		return blockingItems;
	}

}
