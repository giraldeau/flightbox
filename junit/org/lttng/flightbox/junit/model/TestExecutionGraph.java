package org.lttng.flightbox.junit.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.SortedSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.DOTExporter;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraphProviders;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestExecutionGraph {
	
	static String[] testTraces = new String[] {	"trace_fork_exit_simple", 
												"trace_fork_exit_wait"};
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

		ModelBuilder.buildFromStubTrace(trace, model);
		
		WeightedGraph<ExecVertex, ExecEdge> execGraph = listener.getExecGraph();
		saveGraph(execGraph, name);
	}
	
	public void saveGraph(Graph<ExecVertex, ExecEdge> graph, String name) throws IOException {
		DOTExporter<ExecVertex, ExecEdge> dot = ExecGraphProviders.getDOTExporter();
		//OutputStreamWriter writer = new OutputStreamWriter(System.out);
		//dot.export(writer, graph);
		FileWriter fwriter = new FileWriter(new File(Path.getGraphDir(), "exec-graph-" + name + ".dot"));
		dot.export(fwriter, graph);		
	}
	
	@Test
	public void testLongestPath() throws JniException, IOException {
		String trace = "tests/stub/trace_fork_exit_wait.xml";
		SystemModel model = new SystemModel();
		ExecutionTaskListener listener = new ExecutionTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromStubTrace(trace, model);
		
		WeightedGraph<ExecVertex, ExecEdge> execGraph = listener.getExecGraph();
		Task master = model.getLatestTaskByPID(1);
		SortedSet<ExecVertex> set = listener.getTaskVertex(master);

		DijkstraShortestPath<ExecVertex, ExecEdge> dijkstra = new DijkstraShortestPath<ExecVertex, ExecEdge>(execGraph, set.first(), set.last());
		System.out.println(dijkstra.getPath());
		System.out.println(dijkstra.getPathLength());
		
		for (ExecEdge e: execGraph.edgeSet()) {
			ExecVertex v = execGraph.getEdgeSource(e);
			if (v.getType() == ExecVertex.ExecType.BLOCK) {
				execGraph.setEdgeWeight(e, 0.0);
			}
		}
		saveGraph(execGraph, "trace_fork_exit_zero_wait");
		
		DijkstraShortestPath<ExecVertex, ExecEdge> dijkstra2 = new DijkstraShortestPath<ExecVertex, ExecEdge>(execGraph, set.first(), set.last());
		System.out.println(dijkstra2.getPath());
		System.out.println(dijkstra2.getPathLength());
	}
}
