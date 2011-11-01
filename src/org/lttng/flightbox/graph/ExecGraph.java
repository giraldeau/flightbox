package org.lttng.flightbox.graph;

import org.jgrapht.graph.DirectedWeightedMultigraph;

public class ExecGraph extends DirectedWeightedMultigraph<ExecVertex, ExecEdge> {

	private static final long serialVersionUID = 8126102550511947237L;

	public ExecGraph(Class<? extends ExecEdge> arg0) {
		super(arg0);
	}
	public ExecGraph() {
		super(ExecEdge.class);
	}

}
