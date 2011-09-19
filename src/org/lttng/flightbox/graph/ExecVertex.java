package org.lttng.flightbox.graph;

public class ExecVertex implements Comparable<ExecVertex> {

	private String label;
	private static int count = 0;
	private int id;
	private long ts;

	public ExecVertex() {
		this(count++);		
	}
	public ExecVertex(int id) {
		this(id, 0);
	}
	public ExecVertex(int id, long ts) {
		setId(id);
		setTimestamp(ts);
	}
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	@Override
	public int compareTo(ExecVertex o) {
		if (this.ts == o.ts)
			return 0;
		if (this.ts < o.ts)
			return -1;
		return 1;
	}
	public void setTimestamp(long ts) {
		this.ts = ts;
	}
	public long getTimestamp() {
		return ts;
	}

}
