package org.lttng.flightbox;

import java.util.EnumMap;

import org.lttng.flightbox.model.Task.TaskState;

public class TimeStats {

	private double t1;
	private double t2;
	private final EnumMap<TaskState, Double> dataMap;
	public static double NANO = 1000000000;

	public TimeStats() {
		this(0,0);
	}

	public TimeStats(double t1, double t2) {
		this.setStartTime(t1);
		this.setEndTime(t2);
		dataMap = new EnumMap<TaskState, Double>(TaskState.class);
		clear();
	}

	public void clear() {
		for(TaskState mode: TaskState.values()) {
			dataMap.put(mode, 0.0);
		}
	}

	public void addTime(double time, TaskState mode) {
		Double t = dataMap.get(mode);
		t += time;
		dataMap.put(mode, t);
	}

	public double getTime(TaskState mode) {
		return dataMap.get(mode).doubleValue();
	}

	public double getStartTime() {
		return t1;
	}

	public void setStartTime(double t1) {
		this.t1 = t1;
	}

	public double getEndTime() {
		return t2;
	}

	public void setEndTime(double t2) {
		this.t2 = t2;
	}

	public double getSystem() {
		return 	dataMap.get(TaskState.TRAP) +
				dataMap.get(TaskState.SYSCALL) +
				dataMap.get(TaskState.SOFTIRQ) +
				dataMap.get(TaskState.IRQ);
	}

	public double getTotal() {
		return getSystem() + dataMap.get(TaskState.USER);
	}

	public double getDuration() {
		return (t2 - t1);
	}

	public double getIdle() {
		return getDuration() - getTotal();
	}

	public double getTotalAvg() {
		return getTotal() / getDuration();
	}

	public double getAvg(TaskState mode) {
		return dataMap.get(mode) / getDuration();
	}

	public double getIdleAvg() {
		return getIdle() / getDuration();
	}

	public double getSystemAvg() {
		return getSystem() / getDuration();
	}

	public void add(TimeStats other) {
		if (other.getStartTime() < t1){
			this.t1 = other.getStartTime();
		}
		if (other.getEndTime() > t2) {
			this.t2 = other.getEndTime();
		}
		for (TaskState mode: other.dataMap.keySet()) {
			Double d = dataMap.get(mode);
			d += other.dataMap.get(mode);
			dataMap.put(mode, d);
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("enlaps=" + getDuration());
		b.append(" user=" + getTime(TaskState.USER));
		b.append(" system=" + getSystem());
		return b.toString();
	}

	public void addInterval(double x1, double x2, TaskState mode) {
		// verify that this interval is part of our time
		if (x2 <= t1 || x1 >= t2)
			return;

		double t = 0;
		if (x1 >= t1 && x2 <= t2) { // add whole interval
			t = x2 - x1;
		} else if (x1 >= t1 && x2 >= t2) { // starts before
			t = t2 - x1;
		} else if (x1 <= t1 && x2 <= t2) { // end after
			t = x2 - t1;
		} else if (x1 <= t1 && x2 >= t2) { // max time span
			t = t2 - t1;
		}

		addTime(t, mode);

	}

	public TimeStats mul(double factor) {
		for (TaskState mode: dataMap.keySet()) {
			Double d = dataMap.get(mode);
			dataMap.put(mode, d * factor);
		}
		return null;
	}
}
