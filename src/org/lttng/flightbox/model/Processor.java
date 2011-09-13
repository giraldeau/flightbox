package org.lttng.flightbox.model;

import java.util.HashSet;
import java.util.Stack;


/**
 * Model of a computer processor
 *
 * @author francis
 */
public class Processor extends SystemResource implements Comparable<Processor> {

	public enum ProcessorState {
		IDLE, BUSY, TRAP, IRQ, SOFTIRQ
	}

	private boolean isLowPowerMode;
	private int id;
	private final Stack<ProcessorState> state;
	private final HashSet<IProcessorListener> listeners;
	private Task currentTask;

	public Processor(int id) {
		this();
		this.id = id;
	}

	public Processor() {
		listeners = new HashSet<IProcessorListener>();
		state = new Stack<ProcessorState>();
	}

	public boolean isLowPowerMode() {
		return isLowPowerMode;
	}

	public void setLowPowerMode(boolean nextLowPowerMode) {
		fireLowPowerModeChange(nextLowPowerMode);
		this.isLowPowerMode = nextLowPowerMode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ProcessorState peekState() {
		if (state.isEmpty())
			return null;
		return state.peek();
	}

	public void pushState(ProcessorState newState) {
		fireStateChange(newState);
		state.push(newState);
	}

	public void popState() {
		if (state.isEmpty())
			return;
		fireStateChange(state.get(state.size()-1));
		state.pop();
	}

	private void fireStateChange(ProcessorState nextState) {
		for (IProcessorListener l: listeners) {
			l.stateChange(this, nextState);
		}
	}

	private void fireLowPowerModeChange(boolean nextLowPowerMode) {
		for (IProcessorListener l: listeners) {
			l.lowPowerModeChange(this, nextLowPowerMode);
		}
	}

	public void addListener(IProcessorListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IProcessorListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Processor) {
			Processor p = (Processor) other;
			if (p.id == this.id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public int compareTo(Processor other) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (other == this) return EQUAL;
		if (this.id < other.id) return BEFORE;
		if (this.id > other.id) return AFTER;
		return EQUAL;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

}
