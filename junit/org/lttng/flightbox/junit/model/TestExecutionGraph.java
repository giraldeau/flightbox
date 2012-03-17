package org.lttng.flightbox.junit.model;

import java.io.IOException;
import java.util.SortedSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraph;
import org.lttng.flightbox.graph.ExecGraphManager;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.graph.GraphUtils;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.model.LoggingTaskListener;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestExecutionGraph {
	
	static String[] testTraces = new String[] {	"trace_fork_exit_simple", 
												"trace_fork_exit_wait",
												"trace_cpm1"};
	
	static boolean debug = false;
	
	@Test
	public void buildAllExecutionGraph() throws JniException, IOException {
		for (String trace: testTraces) {
			buildExecutionGraphFromStub(trace);
		}
	}
	
	public void buildExecutionGraphFromStub(String name) throws JniException, IOException {
		
		String trace = "tests/stub/" + name + ".xml";
		SystemModel model = new SystemModel();
		ExecutionTaskListener listener = new ExecutionTaskListener();
		model.addTaskListener(listener);
		if (debug) {
			System.out.println("processing " + name);
			model.addTaskListener(new LoggingTaskListener());
		}
		ModelBuilder.buildFromStubTrace(trace, model);
		
		ExecGraph execGraph = ExecGraphManager.getInstance().getGraph();
		GraphUtils.saveGraphDefault(execGraph, name);
	}
	
	/*
	 * ShortestPath can't be used to recover the critical path in the general case.
	 * */
	//@Test
	public void testLongestPath() throws JniException, IOException {
		String trace = "tests/stub/trace_fork_exit_wait.xml";
		SystemModel model = new SystemModel();
		ExecutionTaskListener listener = new ExecutionTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromStubTrace(trace, model);
		
		ExecGraphManager graphManager = ExecGraphManager.getInstance();
		WeightedGraph<ExecVertex, ExecEdge> execGraph = graphManager.getGraph();
		Task master = model.getLatestTaskByPID(1);
		SortedSet<ExecVertex> set = graphManager.getVertexSetForTask(master);

		DijkstraShortestPath<ExecVertex, ExecEdge> dijkstra = new DijkstraShortestPath<ExecVertex, ExecEdge>(execGraph, set.first(), set.last());
		System.out.println(dijkstra.getPath());
		System.out.println(dijkstra.getPathLength());
		
		for (ExecEdge e: execGraph.edgeSet()) {
			ExecVertex v = execGraph.getEdgeSource(e);
			if (v.getType() == ExecVertex.ExecType.BLOCK) {
				execGraph.setEdgeWeight(e, 0.0);
			}
		}
		GraphUtils.saveGraphDefault(execGraph, "trace_fork_exit_zero_wait");
		
		DijkstraShortestPath<ExecVertex, ExecEdge> dijkstra2 = new DijkstraShortestPath<ExecVertex, ExecEdge>(execGraph, set.first(), set.last());
		System.out.println(dijkstra2.getPath());
		System.out.println(dijkstra2.getPathLength());
	}
}
