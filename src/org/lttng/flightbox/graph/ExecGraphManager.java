package org.lttng.flightbox.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lttng.flightbox.model.Task;

/*
 * Shared instance of a graph
 */
public class ExecGraphManager {

	/* singleton */
	private static ExecGraphManager instance = null;
	
	private ExecGraph graph;
	private Map<Task, TreeSet<ExecVertex>> taskVertex;
	
	private ExecGraphManager() {
		graph = new ExecGraph(ExecEdge.class);
		taskVertex = new HashMap<Task, TreeSet<ExecVertex>>();
	}
	
	public static ExecGraphManager getInstance() {
		if (instance == null)
			instance = new ExecGraphManager();
		return instance;
	}
	
	public void appendVertex(ExecVertex v) {
		graph.addVertex(v);
		Task t = v.getTask();
		TreeSet<ExecVertex> vset = taskVertex.get(t);
		if (vset == null) {
			vset = new TreeSet<ExecVertex>();
			taskVertex.put(t, vset);
		}
		if (!vset.isEmpty()) {
			ExecVertex last = vset.last();
			ExecEdge e1 = graph.addEdge(last, v);
			graph.setEdgeWeight(e1, v.getTimestamp() - last.getTimestamp());
		}
		vset.add(v);
	}
	
	public ExecGraph getGraph() {
		return graph;
	}
	
	public SortedSet<ExecVertex> getVertexSetForTask(Task task) {
		return taskVertex.get(task);
	}
	
	public SortedSet<ExecVertex> getOrCreateVertexSetForTask(Task task) {
		TreeSet<ExecVertex> set = taskVertex.get(task);
		if (set == null) {
			set = new TreeSet<ExecVertex>();
			taskVertex.put(task, set);
		}
		return set;
	}

}
