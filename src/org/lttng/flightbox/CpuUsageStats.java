package org.lttng.flightbox;

import java.util.TreeMap;

import org.lttng.flightbox.GlobalState.KernelMode;

public class CpuUsageStats {

	TreeMap<Long, TimeStatsBucket> cpuStats;
	//HashMap<Long, >
	double start;
	double end;
	int nbBuckets;
	
	public CpuUsageStats() {
		cpuStats = new TreeMap<Long, TimeStatsBucket>();
		start = 0; 
		end = 0;
		nbBuckets = 100;
	}
	
	public void addInterval(double ts1, double ts2, Long id, KernelMode mode) {
		if (!cpuStats.containsKey(id)) {
			cpuStats.put(id, new TimeStatsBucket(start, end, nbBuckets));
		}
		
		TimeStatsBucket stat = cpuStats.get(id);
		stat.addInterval(ts1, ts2, mode);
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
