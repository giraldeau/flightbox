package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertTrue;

import java.io.File;
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
	
	@Test
	public void testSimpleExecutionGraph() throws JniException {
		String trace = "inception-3x-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		ExecutionTaskListener listener = new ExecutionTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);
		
		WeightedGraph<ExecVertex, ExecEdge> execGraph = listener.getExecGraph();
		DOTExporter<ExecVertex, ExecEdge> dot = ExecGraphProviders.getDOTExporter();
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		dot.export(writer, execGraph);
	}
	
}
