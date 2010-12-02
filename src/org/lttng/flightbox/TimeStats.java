package org.lttng.flightbox;

import java.util.EnumMap;

import org.lttng.flightbox.GlobalState.KernelMode;

public class TimeStats {

	private double t1;
	private double t2;
	private EnumMap<KernelMode, Double> dataMap;
	
	public TimeStats() {
		this(0,0);
	}
	
	public TimeStats(double t1, double t2) {
		this.setStartTime(t1);
		this.setEndTime(t2);
		dataMap = new EnumMap<KernelMode, Double>(KernelMode.class);
		for(KernelMode mode: KernelMode.values()) {
			dataMap.put(mode, 0.0);
		}
	}
	
	public void addTime(double time, KernelMode mode) {
		Double t = dataMap.get(mode);
		t += time;
		dataMap.put(mode, t);
	}
	
	public double getTime(KernelMode mode) {
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
		return 	dataMap.get(KernelMode.IRQ) +
				dataMap.get(KernelMode.TRAP) +
				dataMap.get(KernelMode.SYSCALL) +
				dataMap.get(KernelMode.SOFTIRQ);
	}
	
	public double getTotal() {
		return getSystem() + dataMap.get(KernelMode.USER);
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
	
	public double getAvg(KernelMode mode) {
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
		for (KernelMode mode: other.dataMap.keySet()) {
			Double d = dataMap.get(mode);
			d += other.dataMap.get(mode);
			dataMap.put(mode, d);
		}
	}
}
