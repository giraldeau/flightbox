package org.lttng.flightbox.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lttng.flightbox.graph.ExecVertex.ExecType;
import org.lttng.flightbox.model.AbstractTaskListener;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.ExitInfo;
import org.lttng.flightbox.model.state.SoftIRQInfo;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;
import org.lttng.flightbox.model.state.WaitInfo;

public class ExecutionTaskListener extends AbstractTaskListener {

	ExecGraph graph;
	Map<Task, TreeSet<ExecVertex>> taskVertex;
	
	public ExecutionTaskListener() {
		graph = new ExecGraph(ExecEdge.class);
		taskVertex = new HashMap<Task, TreeSet<ExecVertex>>();
	}
	
	@Override
	public void pushState(Task task, StateInfo nextState) {
		TaskState taskState = nextState.getTaskState();
		switch (taskState) {
		case ALIVE:
			ExecVertex v1 = new ExecVertex(task, nextState.getStartTime(), ExecType.START);
			appendVertex(v1);
			// link parent
			Task parent = task.getParentProcess();
			if (parent != null) {
				/* append a new vertex to denote the fork */
				ExecVertex v2 = new ExecVertex(parent, nextState.getStartTime(), ExecType.FORK);
				appendVertex(v2);
				/* link the parent and child */
				ExecEdge e = graph.addEdge(v2, v1);
				graph.setEdgeWeight(e, 0.0);
			}
			break;
		case WAIT:
			/* processing for the wait state occurs at wakeup
			 * because we don't know yet if the cause id CPU wait or blocking */
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
			ExecVertex v = new ExecVertex(task, exit.getStartTime(), ExecType.EXIT);
			appendVertex(v);
			break;
		case WAIT:
			WaitInfo wait = (WaitInfo) currState;
			if (!wait.isBlocking())
				break;
			// create two vertex, one at block start and end
			ExecVertex v1 = new ExecVertex(task, wait.getStartTime(), ExecType.BLOCK);
			ExecVertex v2 = new ExecVertex(task, wait.getEndTime(), ExecType.WAKEUP);

			/* append new block and wakeup vertexes to the task graph */
			appendVertex(v1);
			appendVertex(v2);
			linkSubTask(task, wait, v1, v2);
			break;
		default:
			break;
		}
	}

	private void linkSubTask(Task task, WaitInfo wait, ExecVertex v1, ExecVertex v2) {
		// wakeup is the state of the task from which the wakeup occured
		StateInfo state = wait.getWakeUp();
		if (state == null)
			return;
		
		/* the task was waiting directly on a local process
		 * either for process exit or a kernel thread (which is always in SYSCALL state) */
		switch (state.getTaskState()) {
		case EXIT:
		case SYSCALL:
			Task subTask = state.getTask();
			TreeSet<ExecVertex> set = taskVertex.get(subTask);
			if (set != null && !set.isEmpty()) {
				ExecVertex last = set.last();
				ExecEdge e = graph.addEdge(last, v2);
				graph.setEdgeWeight(e, 0);
			}
			break;
		case SOFTIRQ:
			/* Here, the wakeup is indirect. The task on which the SoftIRQ can be anything, 
			 * this it has no relationship with the waked task. 
			 * Queue blocking and wakeup vertexes for post processing */
			SyscallInfo syscall = wait.getWaitingSyscall();
			SoftIRQInfo softirq = (SoftIRQInfo) state;
			if (syscall != null) {
				Integer id = syscall.getSyscallId();
				Integer sirq = softirq.getSoftirqId();
				String sysname = model.getSyscallTable().get(id);
				String sirqname = model.getSoftIRQTable().get(sirq);
				System.out.println("Enqueue syscall:" + sysname + " softirq= " + sirq + " for task " + task + " time " + wait.getEndTime());
			}
			break;
			/*
			int id = waitingSyscall.getSyscallId();
			String name = model.getSyscallTable().get(id);
			if (name.equals("sys_read")) {
				FileDescriptor fd = waitingSyscall.getFileDescriptor();
				if (fd instanceof SocketInet) {
					SocketInet sock = (SocketInet) fd;
					return model.findTaskByComplementSocket(sock);
				}
			}
			*/
		default:
			break;
		}
	}

	private void appendVertex(ExecVertex v) {
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
	
	public ExecGraph getExecGraph() {
		return graph;
	}
	
	public SortedSet<ExecVertex> getTaskVertex(Task task) {
		return taskVertex.get(task);
	}

}
