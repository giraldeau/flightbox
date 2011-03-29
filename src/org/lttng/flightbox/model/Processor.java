package org.lttng.flightbox.model;


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
	
	public Processor(int id) {
		this();
		this.id = id;
	}

	public Processor() {
	}
	
	public boolean isLowPowerMode() {
		return isLowPowerMode;
	}

	public void setLowPowerMode(boolean isLowPowerMode) {
		this.isLowPowerMode = isLowPowerMode;
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
		this.state = state;
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
