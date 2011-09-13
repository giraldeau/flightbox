package org.lttng.flightbox.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.lttng.flightbox.model.ITaskListener;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.AliveInfo;
import org.lttng.flightbox.model.state.StateInfo;

public class ExecutionTaskListener implements ITaskListener {

	WeightedGraph<ExecVertex, ExecEdge> graph;
	Map<Task, TreeSet<ExecVertex>> taskVertex;
	
	public ExecutionTaskListener() {
		graph = new DirectedWeightedMultigraph<ExecVertex, ExecEdge>(ExecEdge.class);
		taskVertex = new HashMap<Task, TreeSet<ExecVertex>>();
	}
	
	@Override
	public void pushState(Task task, StateInfo nextState) {
		if (nextState.getTaskState() == TaskState.ALIVE) {
			ExecVertex v = new ExecVertex();
			graph.addVertex(v);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set == null) {
				set = new TreeSet<ExecVertex>();
				taskVertex.put(task, set);
			}
			set.add(v);
		}
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
		StateInfo currState = task.peekState();
		if (currState.getTaskState() == TaskState.ALIVE) {
			AliveInfo alive = (AliveInfo) currState;
			ExecVertex v = new ExecVertex();
			graph.addVertex(v);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set != null && set.size() > 0) {
				graph.addEdge(set.first(), v);
				graph.setEdgeWeight(graph.getEdge(set.first(), v), (double) alive.getDuration());
			}
		}
	}

	public WeightedGraph<ExecVertex, ExecEdge> getExecGraph() {
		return graph;
	}
	
}
