package org.lttng.flightbox.dep;

import java.util.TreeSet;

import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.AbstractTaskListener;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.WaitInfo;

public class BlockingTaskListener extends AbstractTaskListener {

	private SystemModel model; 
	private BlockingModel blockingModel;
	
	public BlockingTaskListener() {
	}

	@Override
	public void popState(Task task, StateInfo nextState) {

		// should raise exception, but need to think on how to propage it
		if (blockingModel == null)
			return;
			
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

	public void setModel(SystemModel model) {
		this.model = model;
		if (model != null) {
			this.blockingModel = model.getBlockingModel();
		}
	}

	public SystemModel getModel() {
		return model;
	}

}
