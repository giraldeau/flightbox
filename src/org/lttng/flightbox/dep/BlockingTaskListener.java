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

	public BlockingTaskListener() {
		blockingItems = new HashMap<Task, TreeSet<BlockingTree>>();
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
		item.setTask(wait.getTask());

		/* add this blocking item to the process */
		TreeSet<BlockingTree> set = blockingItems.get(task);
		if (set == null) {
			set = new TreeSet<BlockingTree>();
			blockingItems.put(task, set);
		}
		set.add(item);

		/* add references to children blocking items */
		/* FIXME: handle other cases than EXIT */

		/* do it only if the wakeUp signal is known */
		if (wait.getWakeUp() == null)
			return;

		if (wait.getWakeUp().getTaskState() == TaskState.EXIT) {
			TreeSet<BlockingTree> treeSet = blockingItems.get(wait.getWakeUp().getTask());
			if (treeSet != null) {
				BlockingTree b1 = new BlockingTree();
				b1.setStartTime(wait.getStartTime());
				BlockingTree b2 = new BlockingTree();
				b2.setStartTime(wait.getEndTime());
				TreeSet<BlockingTree> treeSet2 = new TreeSet<BlockingTree>();
				treeSet2.addAll(treeSet.subSet(b1, b2));
				item.setChildren(treeSet2);
			}
		}
	}

	public SortedSet<BlockingTree> getBlockingItemsForTask(Task task) {
		SortedSet<BlockingTree> result = null;
		if (blockingItems.containsKey(task))
			result = blockingItems.get(task);
		return result;
	}

}
