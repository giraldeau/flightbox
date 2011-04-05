package org.lttng.flightbox.model;

import org.lttng.flightbox.model.state.StateInfo;


public class TaskListener implements ITaskListener {

	@Override
	public void pushState(Task task, StateInfo nextState) {
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
	}

}
