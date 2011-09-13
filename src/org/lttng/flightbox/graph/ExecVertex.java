package org.lttng.flightbox.graph;

public class ExecVertex implements Comparable<ExecVertex> {

	private String label;
	private static int count = 0;
	private int id;

	public ExecVertex() {
		this(count++);
	}
	public ExecVertex(int id) {
		setId(id);
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
		if (o.id < this.id)
			return -1;
		if (o.id == this.id)
			return 0;
		return 1;
	}

}
