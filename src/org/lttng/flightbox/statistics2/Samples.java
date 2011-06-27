package org.lttng.flightbox.statistics2;

public class Samples {

	long start;
	long end;
	double[] samples;
	
	public Samples(long start, long end, int size) {
		this.start = start;
		this.end = end;
		samples = new double[size]; 
	}
	
	public double sum(long x1, long x2) {
		double total = 0;
		int i = sampleIndex(x1);
		int j = sampleIndex(x2);
		for (int x=i; x<j; x++) {
			total += samples[x];
		}
		return total;
	}
	
	public int sampleIndex(long x) {
		if (x < start || x > end)
			throw new IndexOutOfBoundsException();
		if (x == start)
			return 0;
		if (x == end)
			return samples.length - 1;
		long duration = end - start;
		int index = (int) ((x * samples.length) / duration) - 1;
		return index;
	}
}
