package org.lttng.flightbox.dep;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class ExecutionGraphBuilder {

	public static Graph<ExecVertex, DefaultWeightedEdge> build(Task task, SystemModel model) {
		
		DirectedWeightedMultigraph<ExecVertex, DefaultWeightedEdge> graph =
			new DirectedWeightedMultigraph<ExecVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		BlockingModel bm = model.getBlockingModel();
		return graph;
	}
	
}
