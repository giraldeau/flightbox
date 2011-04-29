package org.lttng.flightbox.statistics;

import java.util.Set;
import java.util.TreeMap;

import org.lttng.flightbox.model.Task.TaskState;

public class ResourceUsage <T> {

	TreeMap<T, BucketSeries> timeStats;
	double start;
	double end;
	int nbBuckets;
	
	public ResourceUsage() {
		this(0L, 0L, 1);
	}
	
	public ResourceUsage(Long start, Long end, int precision) {
		timeStats = new TreeMap<T, BucketSeries>();
		this.start = start; 
		this.end = end;
		nbBuckets = precision;		
	}
	
	public void addInterval(double ts1, double ts2, T id, TaskState mode) {
		if (!timeStats.containsKey(id)) {
			timeStats.put(id, new BucketSeries(start, end, nbBuckets));
		}
		
		BucketSeries stat = timeStats.get(id);
		stat.addInterval(ts1, ts2, mode);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (T cpu : timeStats.keySet()) {
			s.append("id=" + cpu + " " + timeStats.get(cpu) + "\n");
		}
		return s.toString();
	}
	
	public BucketSeries getTotalAvg() {
		BucketSeries total = new BucketSeries(start, end, nbBuckets);
		BucketSeries current;
		// FIXME: should take the numCpu from the trace, we may not have events for all CPUS
		double factor = 1.0 / timeStats.keySet().size();
		Bucket item; 
		for(T id: timeStats.keySet()) {
			current = timeStats.get(id);
			for(int i=0; i<nbBuckets; i++) {
				item = current.getInterval(i);
				total.getInterval(i).add(item);
			}
		}
		total.mul(factor);
		return total;
	}
	
	public BucketSeries getTotal() {
		BucketSeries total = new BucketSeries(start, end, nbBuckets);
		BucketSeries current;
		Bucket item; 
		for(T id: timeStats.keySet()) {
			current = timeStats.get(id);
			for(int i=0; i<nbBuckets; i++) {
				item = current.getInterval(i);
				total.getInterval(i).add(item);
			}
		}
		return total;
	}
	
	public BucketSeries getStats(T id) {
		return timeStats.get(id);
	}
	
	public double getStart() {
		return this.start;
	}
	
	public void setStart(double start) {
		this.start = start;
	}
	
	public double getEnd() {
		return this.end;
	}
	
	public void setEnd(double end) {
		this.end = end;
	}
	
	public double getDuration() {
		return this.end - this.start;
	}
	
	public double[] getXSeries(T id) {
		return timeStats.get(id).getXSeries();
	}
	
	public double[] getYSeries(T id, TaskState mode) {
		return timeStats.get(id).getYSeries(mode);
	}
	
	public int getNumEntry() {
		return timeStats.keySet().size();
	}

	public Set<T> idSet() {
		return timeStats.keySet();
	}
}
