package org.lttng.flightbox.model;

import org.lttng.flightbox.model.state.StateInfo;


public class AbstractTaskListener implements ITaskListener {

	protected SystemModel model;

	@Override
	public void pushState(Task task, StateInfo nextState) {
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
	}

	@Override
	public void setModel(SystemModel model) {
		this.model = model;
	}

}
