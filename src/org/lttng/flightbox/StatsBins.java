package org.lttng.flightbox;

import java.util.ArrayList;

public class StatsBins {

	ArrayList<TimeStats> bins;
	
	double t1;
	double t2;
	double binDuration;
	
	public StatsBins() {
		reset();
	}
	
	public double getStartTime() {
		return t1;
	}

	public double getEndTime() {
		return t2;
	}

	public double getBinDuration() {
		return binDuration;
	}

	
	public TimeStats getSum() {
		if (bins.size() == 0) {
			return new TimeStats();
		}
		TimeStats first = bins.get(0);
		TimeStats sum = new TimeStats(first.getStartTime(), first.getEndTime());
		for (TimeStats t: bins) {
			sum.add(t);
		}
		return sum;
	}
	
	public void init(double t1, double t2, int nbBins) {
		reset();
		this.t1 = t1;
		this.t2 = t2;
		if (nbBins <= 0) {
			return;
		} 
		binDuration = (t2 - t1) / nbBins;
		for(int i=0; i<nbBins; i++) {
			bins.add(new TimeStats(t1 + (binDuration * i), t1 + (binDuration * (i + 1))));
		}
	}
	
	public void reset() {
		bins = new ArrayList<TimeStats>();
	}
	
	public int size() {
		return bins.size();
	}
	
	public TimeStats getIntervalByTime(double t) {
		int index = (int)Math.floor((t - t1) / binDuration);
		return bins.get(index);
	}
	
	// this function should take Alex's intervals and then
	// chunk it into corresponding bins
	public void addInterval() {
		
	}
}
