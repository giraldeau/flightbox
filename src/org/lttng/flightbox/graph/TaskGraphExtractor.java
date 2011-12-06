package org.lttng.flightbox.graph;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;

/*
 * Compute the critical path between start and end vertex. ReverseClosestIterator
 * is used to iterates from the end to the start. 
 */
public class TaskGraphExtractor {
	
	/* returns only the critical path, which is a list of vertex and edges */
	// FIXME: Change return value to GraphPath
	public static ExecSubgraph getCriticalPath(ExecGraph graph, ExecVertex start, ExecVertex end) {
		final Set<ExecVertex> v = new HashSet<ExecVertex>();
		final Set<ExecEdge> e = new HashSet<ExecEdge>();
		ReverseClosestIterator<ExecVertex, ExecEdge> iterator = new ReverseClosestIterator<ExecVertex, ExecEdge>(graph, end);
		TraversalListener<ExecVertex, ExecEdge> listener = new TraversalListener<ExecVertex, ExecEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent event) {
				// FIXME: stop iterating if start and end vertex are not connected
				//iterator.abort();
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent event) { }

			@Override
			public void edgeTraversed(EdgeTraversalEvent<ExecVertex, ExecEdge> event) {
				ExecEdge edge = event.getEdge();
				edge.setCriticalPath(true);
				e.add(edge);
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<ExecVertex> event) {
				// FIXME: if current vertex time is before start vertex and the start vertex
				// has not been encountered, then there is no path between start and end
				//iterator.abort();
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<ExecVertex> event) {
				v.add(event.getVertex());
			}
			
		};
		iterator.addTraversalListener(listener);
		while(iterator.hasNext()) {
			iterator.next();
		}
		ExecSubgraph sub = new ExecSubgraph(graph, v, e);
		return sub;
	}

	/*
	 * Returns the subgraph including all related vertex of the task,
	 * with edges on the critical path annotated
	 */
	public static ExecSubgraph getExecutionGraph(ExecGraph graph, ExecVertex start, ExecVertex end) {
		final Set<ExecVertex> v1 = new HashSet<ExecVertex>();
		final Set<ExecEdge> e1 = new HashSet<ExecEdge>();
		final Set<ExecVertex> v2 = new HashSet<ExecVertex>();
		final Set<ExecEdge> e2 = new HashSet<ExecEdge>();
		
		EdgeReversedGraph<ExecVertex, ExecEdge> revGraph = new EdgeReversedGraph<ExecVertex, ExecEdge>(graph);
		
		ClosestFirstIterator<ExecVertex, ExecEdge> iterator1 = new ClosestFirstIterator<ExecVertex, ExecEdge>(revGraph, end);
		ClosestFirstIterator<ExecVertex, ExecEdge> iterator2 = new ClosestFirstIterator<ExecVertex, ExecEdge>(graph, start);
		
		InventoryTraversalListener<ExecVertex, ExecEdge> inventoryReverse = new InventoryTraversalListener<ExecVertex, ExecEdge>(v1, e1);
		InventoryTraversalListener<ExecVertex, ExecEdge> inventoryForward = new InventoryTraversalListener<ExecVertex, ExecEdge>(v2, e2);
		
		iterator1.addTraversalListener(inventoryReverse);
		while(iterator1.hasNext()) {
			iterator1.next();
		}
		
		iterator2.addTraversalListener(inventoryForward);
		while(iterator2.hasNext()) {
			iterator2.next();
		}
		
		/* intersection of sets */
		v1.retainAll(v2);
		e1.retainAll(e2);
		ExecSubgraph sub = new ExecSubgraph(graph, v1, e1);
		
		/* annotate the critical path */
		getCriticalPath(graph, start, end);
		
		return sub;
	}
	
}
