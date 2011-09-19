package org.lttng.flightbox.junit.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.DOTExporter;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraphProviders;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;

public class TestExecutionGraph {
	
	@Test
	public void testSimpleExecutionGraph() throws JniException, IOException {
		String trace = "tests/stub/process_fork_exit.xml";
		SystemModel model = new SystemModel();
		ExecutionTaskListener listener = new ExecutionTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromStubTrace(trace, model);
		
		WeightedGraph<ExecVertex, ExecEdge> execGraph = listener.getExecGraph();
		Set<ExecVertex> toRemove = new HashSet<ExecVertex>();
		// prune the graph of all not connected vertex
		for (ExecVertex v: execGraph.vertexSet()) {
			if (execGraph.edgesOf(v).isEmpty()) {
				toRemove.add(v);
			}
		}
		System.out.println("remove " + toRemove.size() + " items");
		//execGraph.removeAllVertices(toRemove);
		
		DOTExporter<ExecVertex, ExecEdge> dot = ExecGraphProviders.getDOTExporter();
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		dot.export(writer, execGraph);
		FileWriter fwriter = new FileWriter(new File(Path.getGraphDir(), "exec-graph-fork-exit.dot"));
		dot.export(fwriter, execGraph);
	}
}
