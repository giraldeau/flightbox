package org.lttng.flightbox.dep;

import java.util.TreeSet;

import org.lttng.flightbox.model.AbstractTaskListener;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.WaitInfo;

public class BlockingTaskListener extends AbstractTaskListener {

	private BlockingModel blockingModel;
	
	public BlockingTaskListener() {
	}

	@Override
	public void popState(Task task, StateInfo nextState) {

		if (blockingModel == null) {
			blockingModel = getBlockingModel();
			if (blockingModel == null)
				throw new RuntimeException("BlockingModel not found");
		}
			
		StateInfo info = task.peekState();
		if (info.getTaskState() != TaskState.WAIT)
			return;

		WaitInfo wait = (WaitInfo) info;
		if (!wait.isBlocking())
			return;

		BlockingItem item = new BlockingItem();
		/* copy relevant data */
		item.setStartTime(wait.getStartTime());
		item.setEndTime(wait.getEndTime());
		item.setWaitingSyscall(wait.getWaitingSyscall());
		item.setWakeUp(wait.getWakeUp());
		item.setWakeUpTask(wait.getWakeUpTask());
		item.setTask(wait.getTask());

		/* add this blocking item to the process */
		TreeSet<BlockingItem> set = blockingModel.getBlockingItemsForTask(task);
		set.add(item);

	}

	public BlockingModel getBlockingModel() {
		BlockingModel m = null;
		if (model != null) {
			m = model.getBlockingModel();
		}
		return m;
	}
}
