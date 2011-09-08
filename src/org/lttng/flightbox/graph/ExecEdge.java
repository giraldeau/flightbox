package org.lttng.flightbox.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class ExecEdge extends DefaultWeightedEdge {
	
	private static final long serialVersionUID = 183736193043773L;
	
	private String label;

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
