package org.lttng.flightbox.state;

public class Interval {

	public Long t1;
	public Long t2;
	public Object content;
	
	public String toString() {
		return "(" + t1 + "," + t2 + "," + content.toString() + ")";
	}
}
