package org.lttng.flightbox;

import java.util.HashMap;

import org.lttng.flightbox.GlobalState.KernelMode;

public class CpuUsageStats {

	HashMap<Long, TimeStats> cpuStats;
	//HashMap<Long, >
	double start;
	double end;
	
	public CpuUsageStats() {
		cpuStats = new HashMap<Long, TimeStats>();
		start = 0; 
		end = 0;
	}
	
	public void addInterval(double ts1, double ts2, Long id, KernelMode mode){
		if (!cpuStats.containsKey(id)) {
			cpuStats.put(id, new TimeStats(start, end));
		}
		
		TimeStats stat = cpuStats.get(id);
		stat.addTime(ts2 - ts1, mode);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Long cpu : cpuStats.keySet()) {
			s.append("cpu=" + cpu + " " + cpuStats.get(cpu) + "\n");
		}
		return s.toString();
	}
	
	public TimeStats getTotal() {
		TimeStats total = new TimeStats();
		for(Long cpu: cpuStats.keySet()) {
			total.add(cpuStats.get(cpu));
		}
		return total;
	}
	
	public void setStart(double start) {
		this.start = start;
	}
	public void setEnd(double end) {
		this.end = end;
	}
}
