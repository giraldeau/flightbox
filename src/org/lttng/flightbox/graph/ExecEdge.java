package org.lttng.flightbox.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class ExecEdge extends DefaultWeightedEdge {
	
	private static final long serialVersionUID = 183736193043773L;
	
	private String label;
	private boolean isCriticalPath;
	
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getWeight() {
		return super.getWeight();
	}

	public void setCriticalPath(boolean isCriticalPath) {
		this.isCriticalPath = isCriticalPath;
	}

	public boolean isCriticalPath() {
		return isCriticalPath;
	}
}
