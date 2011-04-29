package org.lttng.flightbox.statistics;

import java.util.ArrayList;

import org.lttng.flightbox.model.Task.TaskState;

public class BucketSeries {

	ArrayList<Bucket> buckets;
	
	double t1;
	double t2;
	double bucketDuration;
	
	public BucketSeries() {
		this(0, 0, 0);
	}
	
	public BucketSeries(double t1, double t2, int nbBucket) {
		init(t1, t2, nbBucket);
	}

	
	public double getStartTime() {
		return t1;
	}

	public double getEndTime() {
		return t2;
	}

	public double getBinDuration() {
		return bucketDuration;
	}

	
	public Bucket getSum() {
		if (buckets.size() == 0) {
			return new Bucket();
		}
		Bucket first = buckets.get(0);
		Bucket last = buckets.get(buckets.size()-1);
		Bucket sum = new Bucket(first.getStartTime(), last.getEndTime());
		for (Bucket t: buckets) {
			sum.add(t);
		}
		return sum;
	}
	
	/**
	 * getSum between given interval
	 */
	public Bucket getSum(double t1, double t2) {
		if (buckets.size() == 0) {
			return new Bucket();
		}
		int x1 = getIntervalIndex(t1);
		int x2 = getIntervalIndex(t2);
		Bucket first = getIntervalByTime(t1);
		Bucket last = getIntervalByTime(t2);
		Bucket sum = new Bucket(first.getStartTime(), last.getEndTime());
		for (int i=x1;i<x2;i++) {
			sum.add(buckets.get(i));
		}
		return sum;
	}
	
	public void init(double t1, double t2, int nbBucket) {
		buckets = new ArrayList<Bucket>();
		this.t1 = t1;
		this.t2 = t2;
		if (nbBucket <= 0) {
			bucketDuration = 0;
			return;
		} 
		bucketDuration = (t2 - t1) / nbBucket;
		for(int i=0; i<nbBucket; i++) {
			buckets.add(new Bucket(t1 + (bucketDuration * i), t1 + (bucketDuration * (i + 1))));
		}
	}
	
	public int size() {
		return buckets.size();
	}
	
	public Bucket getIntervalByTime(double t) {
		int index = getIntervalIndex(t);
		return buckets.get(index);
	}
	
	public int getIntervalIndex(double t) {
		if (bucketDuration == 0) {
			return 0;
		}
		int index = (int)Math.floor((t - t1) / bucketDuration);
		if (index < 0) {
			return 0;
		} else if (index >= buckets.size()) {
			return buckets.size() - 1;
		}
		return index;
	}
	
	public void addInterval(double t1, double t2, TaskState mode) {
		int index_start = getIntervalIndex(t1);
		int index_end = getIntervalIndex(t2);
		for(int i=index_start; i<=index_end; i++) {
			buckets.get(i).addInterval(t1, t2, mode);
		}
	}

	public Bucket getInterval(int index) {
		return buckets.get(index);
	}
	
	public double[] getXSeries() {
		double[] dataX = new double[buckets.size()];
		for(int i=0; i<buckets.size(); i++) {
			dataX[i] = getInterval(i).getStartTime();
		}
		return dataX;
	}
	
	public double[] getYSeries(TaskState mode) {
		double[] dataY = new double[buckets.size()];
		for(int i=0; i<buckets.size(); i++) {
			dataY[i] = getInterval(i).getAvg(mode);
		}
		return dataY;
	}

	public void mul(double factor) {
		for(Bucket stat: buckets) {
			stat.mul(factor);
		}
	}
	
	public String toString() {
		return getSum().toString();
	}
	
}
