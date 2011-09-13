package org.lttng.flightbox.junit.model;

import java.io.OutputStreamWriter;

import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.DOTExporter;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraphProviders;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ExecutionTaskListener;
import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.state.AliveInfo;

public class TestExecutionGraphSimple {

	@Test
	public void testForkExit() {
		Task task = new Task();
		AliveInfo info1 = new AliveInfo();
		
		ExecutionTaskListener listener = new ExecutionTaskListener();
		task.addListener(listener);
		
		task.pushState(info1);
		task.popState();
		
		WeightedGraph<ExecVertex, ExecEdge> execGraph = listener.getExecGraph();
		DOTExporter<ExecVertex, ExecEdge> dotExporter = ExecGraphProviders.getDOTExporter();
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		dotExporter.export(writer, execGraph);
		
	}
	
}
