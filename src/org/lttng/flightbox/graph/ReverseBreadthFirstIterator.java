package org.lttng.flightbox.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.CrossComponentIterator;

public class ReverseBreadthFirstIterator<V, E> extends CrossComponentIterator<V, E, Object> {

	protected Deque<V> queue = new ArrayDeque<V>();
	protected V startVertex;
	protected DirectedGraph<V, E> graph;
	
	/* we start by the last vertex */
	public ReverseBreadthFirstIterator(DirectedGraph<V, E> g, V startVertex) {
		super(g, startVertex);
		this.startVertex = startVertex;
		this.graph = g;
		setCrossComponentTraversal(false);
	}

	@Override
	protected void encounterVertex(V vertex, E edge) {
		putSeenData(vertex, null);
		queue.add(vertex);
	}

	@Override
	protected void encounterVertexAgain(V vertex, E edge) {
	}

	@Override
	protected boolean isConnectedComponentExhausted() {
		return queue.isEmpty();
	}

	@Override
	protected V provideNextVertex() {
		return queue.removeFirst();
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
			addUnseenChildrenOf(nextVertex);
			return nextVertex;
		} else {
			throw new NoSuchElementException();
		}
	}
	
	@Override
	public boolean hasNext() {
		if (startVertex != null) {
			encounterVertex(startVertex, null);
			startVertex = null;
		}
		return !queue.isEmpty();
	}
	
	private void addUnseenChildrenOf(V vertex) {
		for (E edge: graph.incomingEdgesOf(vertex)) {
			if (nListeners != 0) {
				fireEdgeTraversed(createEdgeTraversalEvent(edge));
			}
			
			V other = Graphs.getOppositeVertex(graph, edge, vertex);
			if (isSeenVertex(other)) {
				encounterVertexAgain(other, edge);
			} else {
				encounterVertex(other, edge);
			}
		}
	}
	
	protected EdgeTraversalEvent<V, E> createEdgeTraversalEvent(E edge) {
		return new EdgeTraversalEvent<V, E>(this, edge);
	}
	
	protected VertexTraversalEvent<V> createVertexTraversalEvent(V vertex) {
		return new VertexTraversalEvent<V>(this, vertex);
	}
}
