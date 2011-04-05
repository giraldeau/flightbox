package org.lttng.flightbox.model;

import org.lttng.flightbox.model.state.StateInfo;


public interface ITaskListener {

	public void pushState(Task task, StateInfo nextState);

	public void popState(Task task, StateInfo nextState);

}
