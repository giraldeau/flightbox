package org.lttng.flightbox.graph;

import java.util.Set;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DirectedWeightedSubgraph;

public class ExecSubgraph extends DirectedWeightedSubgraph<ExecVertex, ExecEdge> {

	private static final long serialVersionUID = 8336900100533291665L;
	
	public ExecSubgraph(WeightedGraph<ExecVertex, ExecEdge> arg0,
			Set<ExecVertex> arg1, Set<ExecEdge> arg2) {
		super(arg0, arg1, arg2);
	}

}
