package org.lttng.flightbox.graph;

import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;

public class ReverseClosestIterator<V, E> extends ReverseBreadthFirstIterator<V, E> {

	public ReverseClosestIterator(DirectedGraph<V, E> g, V startVertex) {
		super(g, startVertex);
	}

	@Override
	public V next() {
		if (startVertex != null) {
			encounterVertex(startVertex, null);
			startVertex = null;
		}
		if (hasNext()) {
			V nextVertex = provideNextVertex();
			if (nListeners != 0)
				fireVertexTraversed(createVertexTraversalEvent(nextVertex));
			addClosestChildrenOf(nextVertex);
			return nextVertex;
		} else {
			throw new NoSuchElementException();
		}
	}

	private void addClosestChildrenOf(V vertex) {
		Set<E> incomingEdges = graph.incomingEdgesOf(vertex);
		if (incomingEdges == null)
			return;
		// find the closest edge
		E closest = null;
		double min = Double.MAX_VALUE;
		for (E edge: incomingEdges) {
			double edgeWeight = graph.getEdgeWeight(edge);
			if (edgeWeight < 0.0) {
				throw new IllegalArgumentException("negative weight not allowed");
			}
			if (edgeWeight < min) {
				closest = edge;
				min = edgeWeight;
			}
		}
		if (closest == null)
			return;
	
		if (nListeners != 0) {
			fireEdgeTraversed(createEdgeTraversalEvent(closest));
		}
		
		V other = Graphs.getOppositeVertex(graph, closest, vertex);
		if (isSeenVertex(other)) {
			encounterVertexAgain(other, closest);
		} else {
			encounterVertex(other, closest);
		}
	}
}
