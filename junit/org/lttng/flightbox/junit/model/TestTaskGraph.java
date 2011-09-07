package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.DependencyGraphBuilder;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestTaskGraph {
	
	@Test
	public void testGraphLib() {
		Task t1 = new Task(1);
		Task t2 = new Task(2);
		
		SimpleGraph<Task, DefaultEdge> graph = new SimpleGraph<Task, DefaultEdge>(DefaultEdge.class);
		graph.addVertex(t1);
		graph.addVertex(t2);
		graph.addEdge(t1, t2);
		assertEquals(1, graph.edgesOf(t1).size());
		assertEquals(1, graph.edgesOf(t2).size());
	}
	
	class MyEdge {
		public String label;
		public MyEdge(String label) {
			this.label = label;
		}
	}
	
	@Test
	public void testGraphVizExporter() throws IOException {
		Task t1 = new Task(1);
		t1.setCmd("one");
		Task t2 = new Task(2);
		t2.setCmd("two");
		Task t3 = new Task(3);
		t3.setCmd("three");
		
		EdgeFactory<Task, MyEdge> edgeFactory = new EdgeFactory<Task, MyEdge>() {
			@Override
			public MyEdge createEdge(Task arg0, Task arg1) {
				return new MyEdge("some edge");
			}
		};
		
		SimpleGraph<Task, MyEdge> graph = new SimpleGraph<Task, MyEdge>(edgeFactory);
		graph.addVertex(t1);
		graph.addVertex(t2);
		graph.addVertex(t3);
		graph.addEdge(t1, t2);
		graph.addEdge(t1, t3);
		graph.addEdge(t2, t3);

		VertexNameProvider<Task> vertexIDProvider = new VertexNameProvider<Task>() {
			@Override
			public String getVertexName(Task arg0) {
				return String.format("%d", arg0.getProcessId());
			}
		};
		
		VertexNameProvider<Task> vertexNameProvider = new VertexNameProvider<Task>() {
			@Override
			public String getVertexName(Task arg0) {
				return arg0.getCmd();
			}
		};
		
		EdgeNameProvider<MyEdge> edgeNameProvider = new EdgeNameProvider<MyEdge>() {
			@Override
			public String getEdgeName(MyEdge arg0) {
				return arg0.label;
			}
		};
		
		ComponentAttributeProvider<Task> vertexAttributeProvider = new ComponentAttributeProvider<Task>() {
			@Override
			public Map<String, String> getComponentAttributes(Task arg0) {
				return new HashMap<String, String>();
			}
		};
		
		ComponentAttributeProvider<MyEdge> edgeAttributeProvider = new ComponentAttributeProvider<MyEdge>() {
			@Override
			public Map<String, String> getComponentAttributes(MyEdge arg0) {
				return new HashMap<String, String>();
			}
		};

		/* 
		 * must provide vertex and edge attribute provider, otherwise null
		 * pointer exception is raised. Could it be a bug?
		 */
		/*
		DOTExporter<Task, MyEdge> exporter = new DOTExporter<Task, MyEdge>(
				vertexIDProvider, vertexNameProvider, edgeNameProvider);
		*/
		
		DOTExporter<Task, MyEdge> exporter = new DOTExporter<Task, MyEdge>(
				vertexIDProvider, vertexNameProvider, edgeNameProvider, 
				vertexAttributeProvider, edgeAttributeProvider);

		FileWriter writer = new FileWriter(new File(Path.getGraphDir(), "test-graph.dot"));
		exporter.export(writer, graph);
		writer.close();
	}
	
	@Test
	public void testClientServerRelationGraph() throws JniException, IOException {
		testRelationGraph("rpc-hog-100ms", "clihog");
	}

	@Test
	public void testParentChildRelationGraph() throws JniException, IOException {
		testRelationGraph("inception-3x-100ms", "inception");
	}

	public void testRelationGraph(String trace, String binary) throws JniException, IOException {
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		TreeSet<Task> taskSet = model.getTaskByCmdBasename(binary);
		Graph<Task, DefaultEdge> graph = DependencyGraphBuilder.build(taskSet.first(), model);
		DependencyGraphBuilder.printGraph(graph);
		
		File outDir = Path.getGraphDir();
		FileWriter writer = new FileWriter(new File(outDir, trace + "-task-graph.dot"));
		DependencyGraphBuilder.toDot(writer, graph);
		writer.close();		
	}
	
}
