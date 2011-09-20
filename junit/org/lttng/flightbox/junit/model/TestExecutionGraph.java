package org.lttng.flightbox.junit.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
		DOTExporter<ExecVertex, ExecEdge> dot = ExecGraphProviders.getDOTExporter();
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		dot.export(writer, execGraph);
		FileWriter fwriter = new FileWriter(new File(Path.getGraphDir(), "exec-graph-" + name + ".dot"));
		dot.export(fwriter, execGraph);
	}
}
