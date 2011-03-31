package org.lttng.flightbox.model;

import java.util.List;
import java.util.Vector;


/**
 * Model of a computer processor
 *
 * @author francis
 */
public class Processor implements Comparable<Processor> {

	public enum ProcessorState {
		USER, SYSCALL, TRAP, IRQ, SOFTIRQ, IDLE
	}

	private boolean isLowPowerMode;
	private int id;
	private ProcessorState state;
	private List<IProcessorListener> listeners;
	
	public Processor(int id) {
		this();
		this.id = id;
	}

	public Processor() {
		listeners = new Vector<IProcessorListener>();
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

	public ProcessorState getState() {
		return state;
	}

	public void setState(ProcessorState state) {
		fireStateChange(state);
		this.state = state;
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
	
	public boolean equals(Object other) {
		if (other instanceof Processor) {
			Processor p = (Processor) other;
			if (p.id == this.id) {
				return true;
			}
		}
		return false;
	}
	
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
	
}
