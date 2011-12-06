package org.lttng.flightbox.graph;

import java.util.Set;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

public class InventoryTraversalListener<V, E> implements TraversalListener<V, E> {

	private Set<V> v;
	private Set<E> e;
	
	public InventoryTraversalListener(Set<V> v, Set<E> e) {
		this.v = v;
		this.e = e;
	}
	
	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<V, E> event) {
		e.add(event.getEdge());
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<V> event) {
		v.add(event.getVertex());
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<V> event) {
	}

}
