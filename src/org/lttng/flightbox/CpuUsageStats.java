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
		this(0L, 0L, 1);
	}
	
	public CpuUsageStats(Long start, Long end, int precision) {
		cpuStats = new TreeMap<Long, TimeStatsBucket>();
		this.start = start; 
		this.end = end;
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
	
	public TimeStatsBucket getTotal() {
		TimeStatsBucket total = new TimeStatsBucket(start, end, nbBuckets);
		TimeStatsBucket current;
		for(Long id: cpuStats.keySet()) {
			current = cpuStats.get(id);
			for(int i=0; i<nbBuckets; i++) {
				total.getInterval(i).add(current.getInterval(i));
			}
		}
		return total;
	}
	
	public TimeStatsBucket getStats(Long id) {
		return cpuStats.get(id);
	}
	
	public void setStart(double start) {
		this.start = start;
	}
	public void setEnd(double end) {
		this.end = end;
	}
}
