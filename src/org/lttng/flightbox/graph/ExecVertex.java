package org.lttng.flightbox.graph;

import org.lttng.flightbox.model.Task;

public class ExecVertex implements Comparable<ExecVertex> {

	public enum ExecType {
		UNKNOWN, BLOCK, WAKEUP, FORK, START, EXIT
	}
	
	private String label;
	private static int count = 0;
	private int id;
	private long ts;
	private ExecType type;
	private Task task;
	private boolean resolved;

	public ExecVertex() {
		this(count++);
	}
	public ExecVertex(int id) {
		this(id, 0);
	}
	public ExecVertex(int id, long ts) {
		setId(id);
		setTimestamp(ts);
		setType(ExecType.UNKNOWN);
		setResolved(true);
	}
	public ExecVertex(Task task, long ts, ExecType exe) {
		setId(count++);
		setTask(task);
		setTimestamp(ts);
		setType(exe);
		setResolved(true);
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
	
	@Override
	public String toString() {
		return String.format("%d", this.id);
	}
	public ExecType getType() {
		return type;
	}
	public void setType(ExecType type) {
		this.type = type;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public boolean isResolved() {
		return resolved;
	}
	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}
}
