package org.lttng.flightbox.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.lttng.flightbox.graph.ExecVertex.ExecType;
import org.lttng.flightbox.model.ITaskListener;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.WaitInfo;

public class ExecutionTaskListener implements ITaskListener {

	WeightedGraph<ExecVertex, ExecEdge> graph;
	Map<Task, TreeSet<ExecVertex>> taskVertex;
	
	public ExecutionTaskListener() {
		graph = new DirectedWeightedMultigraph<ExecVertex, ExecEdge>(ExecEdge.class);
		taskVertex = new HashMap<Task, TreeSet<ExecVertex>>();
	}
	
	@Override
	public void pushState(Task task, StateInfo nextState) {
		TaskState taskState = nextState.getTaskState();
		switch (taskState) {
		case ALIVE:
			ExecVertex v1 = new ExecVertex();
			v1.setTimestamp(nextState.getStartTime());
			v1.setType(ExecType.START);
			v1.setTask(task);
			graph.addVertex(v1);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set == null) {
				set = new TreeSet<ExecVertex>();
				taskVertex.put(task, set);
			}
			set.add(v1);
			// link parent
			Task parent = task.getParentProcess();
			if (parent != null) {
				ExecVertex v2 = new ExecVertex();
				v2.setTimestamp(nextState.getStartTime());
				v2.setType(ExecType.FORK);
				v2.setTask(parent);
				graph.addVertex(v2);
				set = taskVertex.get(parent);
				if (set == null) {
					set = new TreeSet<ExecVertex>();
					taskVertex.put(parent, set);
				}
				if (!set.isEmpty()) {
					ExecVertex last = set.last();
					ExecEdge e = graph.addEdge(last, v2);
					graph.setEdgeWeight(e, nextState.getStartTime() - last.getTimestamp());
				}
				set.add(v2);
				ExecEdge e = graph.addEdge(v2, v1);
				graph.setEdgeWeight(e, 0.0);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void popState(Task task, StateInfo nextState) {
		StateInfo currState = task.peekState();
		TaskState taskState = currState.getTaskState();
		switch (taskState) {
		case EXIT :
			ExitInfo exit = (ExitInfo) currState;
			ExecVertex v = new ExecVertex();
			v.setTimestamp(exit.getStartTime());
			v.setType(ExecType.EXIT);
			v.setTask(task);
			graph.addVertex(v);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set == null) {
				set = new TreeSet<ExecVertex>();
				taskVertex.put(task, set);
			}
			if (set != null && set.size() > 0) {
				ExecVertex last = set.last();
				ExecEdge e = graph.addEdge(last, v);
				graph.setEdgeWeight(e, exit.getEndTime() - last.getTimestamp());
			}
			set.add(v);
			break;
		case WAIT:
			WaitInfo wait = (WaitInfo) currState;
			if (!wait.isBlocking())
				break;
			// create two vertex, one at block start and end
			ExecVertex v1 = new ExecVertex();
			ExecVertex v2 = new ExecVertex();
			v1.setTimestamp(wait.getStartTime());
			v2.setTimestamp(wait.getEndTime());
			v1.setType(ExecType.BLOCK);
			v2.setType(ExecType.WAKEUP);
			v2.setLabel("test");
			v1.setTask(task);
			v2.setTask(task);
			graph.addVertex(v1);
			graph.addVertex(v2);
			TreeSet<ExecVertex> vset = taskVertex.get(task);
			if (vset == null) {
				vset = new TreeSet<ExecVertex>();
				taskVertex.put(task, vset);
			}
			if (!vset.isEmpty()) {
				ExecVertex last = vset.last();
				ExecEdge e1 = graph.addEdge(last, v1);
				ExecEdge e2 = graph.addEdge(v1, v2);
				graph.setEdgeWeight(e1, wait.getStartTime() - last.getTimestamp());
				graph.setEdgeWeight(e2, wait.getDuration());
			}
			vset.add(v1);
			vset.add(v2);
			linkSubTask(task, wait, v1, v2);
			break;
		default:
			break;
		}
	}

	private void linkSubTask(Task task, WaitInfo wait, ExecVertex v1, ExecVertex v2) {
		Task subTask = wait.getWakeUp().getTask();		
		/* the task was waiting on a process, assuming the other task already exited */
		if (wait.getWakeUp().getTaskState() == TaskState.EXIT) {
			TreeSet<ExecVertex> set = taskVertex.get(subTask);
			ExecVertex last = set.last();
			ExecEdge e = graph.addEdge(last, v2);
			graph.setEdgeWeight(e, 0);
		}
	}

	public WeightedGraph<ExecVertex, ExecEdge> getExecGraph() {
		return graph;
	}
	
	public SortedSet<ExecVertex> getTaskVertex(Task task) {
		return taskVertex.get(task);
	}
}
