package org.lttng.flightbox.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
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
			ExecVertex v = new ExecVertex();
			v.setTimestamp(nextState.getStartTime());
			v.setLabel(task.toString() + " fork");
			graph.addVertex(v);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set == null) {
				set = new TreeSet<ExecVertex>();
				taskVertex.put(task, set);
			}
			set.add(v);
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
			v.setLabel(task.toString() + " exit");
			graph.addVertex(v);
			TreeSet<ExecVertex> set = taskVertex.get(task);
			if (set == null) {
				set = new TreeSet<ExecVertex>();
				taskVertex.put(task, set);
			}
			if (set != null && set.size() > 0) {
				graph.addEdge(set.last(), v);
				graph.setEdgeWeight(graph.getEdge(set.last(), v), (double) exit.getDuration());
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
			v1.setLabel(task.toString() + " wait");
			v2.setLabel(task.toString() + " wake");
			graph.addVertex(v1);
			graph.addVertex(v2);
			TreeSet<ExecVertex> vset = taskVertex.get(task);
			if (vset == null) {
				vset = new TreeSet<ExecVertex>();
				taskVertex.put(task, vset);
			}
			if (vset != null && vset.size() > 0) {
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
		/* link the parent and the child, do it only in case of a wait to avoid noise */
		if (subTask.getParentProcess() == task) {
			TreeSet<ExecVertex> set = taskVertex.get(subTask);
			graph.addEdge(v1, set.first());
		}
		
		/* the task was waiting on a process, assuming the other task already exited */
		if (wait.getWakeUp().getTaskState() == TaskState.EXIT) {
			TreeSet<ExecVertex> set = taskVertex.get(subTask);
			graph.addEdge(set.last(), v2);
		}
	}

	public WeightedGraph<ExecVertex, ExecEdge> getExecGraph() {
		return graph;
	}
	
}
