package org.lttng.flightbox;

import java.util.TreeMap;

import org.lttng.flightbox.GlobalState.KernelMode;

public class UsageStats <T> {

	TreeMap<T, TimeStatsBucket> cpuStats;
	double start;
	double end;
	int nbBuckets;
	
	public UsageStats() {
		this(0L, 0L, 1);
	}
	
	public UsageStats(Long start, Long end, int precision) {
		cpuStats = new TreeMap<T, TimeStatsBucket>();
		this.start = start; 
		this.end = end;
		nbBuckets = precision;		
	}
	
	public void addInterval(double ts1, double ts2, T id, KernelMode mode) {
		if (!cpuStats.containsKey(id)) {
			cpuStats.put(id, new TimeStatsBucket(start, end, nbBuckets));
		}
		
		TimeStatsBucket stat = cpuStats.get(id);
		stat.addInterval(ts1, ts2, mode);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (T cpu : cpuStats.keySet()) {
			s.append("cpu=" + cpu + " " + cpuStats.get(cpu) + "\n");
		}
		return s.toString();
	}
	
	public TimeStatsBucket getTotal() {
		TimeStatsBucket total = new TimeStatsBucket(start, end, nbBuckets);
		TimeStatsBucket current;
		// FIXME: should take the numCpu from the trace, we may not have events for all CPUS
		double factor = 1.0 / cpuStats.keySet().size();
		TimeStats item; 
		for(T id: cpuStats.keySet()) {
			current = cpuStats.get(id);
			for(int i=0; i<nbBuckets; i++) {
				item = current.getInterval(i);
				item.mul(factor);
				total.getInterval(i).add(item);
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
	
	public double[] getXSeries(Long id) {
		return cpuStats.get(id).getXSeries();
	}
	
	public double[] getYSeries(Long id, KernelMode mode) {
		return cpuStats.get(id).getYSeries(mode);
	}
	
	public int getNumEntry() {
		return cpuStats.keySet().size();
	}
}
