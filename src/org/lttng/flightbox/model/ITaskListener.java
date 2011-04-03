package org.lttng.flightbox.model;


public interface ITaskListener {

	public void pushState(Task task, StateInfo nextState);

	public void popState(Task task, StateInfo nextState);

}
