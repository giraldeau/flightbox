package org.lttng.flightbox.graph;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;


public class TaskGraphExtractor {
	
	public static ExecSubgraph getCriticalPath(ExecGraph graph, ExecVertex start, ExecVertex end) {
		return getCriticalPathAnnotate(graph, start, end, false);
	}
	
	public static ExecSubgraph getCriticalPathAnnotate(ExecGraph graph, ExecVertex start, ExecVertex end, boolean annotate) {
		final Set<ExecVertex> v = new HashSet<ExecVertex>();
		final Set<ExecEdge> e = new HashSet<ExecEdge>();
		final boolean isAnnotate = annotate;
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
				if (isAnnotate)
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
	
}
