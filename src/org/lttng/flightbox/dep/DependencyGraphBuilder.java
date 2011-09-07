package org.lttng.flightbox.dep;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class DependencyGraphBuilder {

	public static Graph<Task, DefaultEdge> build(Task task, SystemModel model) {
		DirectedMultigraph<Task, DefaultEdge> graph = new DirectedMultigraph<Task, DefaultEdge>(DefaultEdge.class);
		BlockingModel bm = model.getBlockingModel();
		TreeSet<BlockingItem> items = bm.getBlockingItemsForTask(task);
		populateGraph(graph, task, items, model);
		return graph;
	}

	private static void populateGraph(Graph<Task, DefaultEdge> graph, Task task,
			TreeSet<BlockingItem> items, SystemModel model) {
		
		for (BlockingItem item: items) {
			Task subTask = item.getSubTask(model);
			if (subTask == null)
				continue;
			graph.addVertex(task);
			graph.addVertex(subTask);
			if (graph.getEdge(task, subTask) == null) {
				graph.addEdge(task, subTask);
			}
			populateGraph(graph, subTask, item.getChildren(model), model);
		}		
	}
	
	public static void printGraph(Graph<Task, DefaultEdge> graph) {
        Iterator<Task> iter =
            new DepthFirstIterator<Task, DefaultEdge>(graph);
        Task vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            System.out.println(
                "Vertex " + vertex.toString() + " is connected to: "
                + graph.edgesOf(vertex).toString());
        }
	}

	public static void toDot(Writer writer, Graph<Task, DefaultEdge> graph) {
		
		VertexNameProvider<Task> vertexIDProvider = new VertexNameProvider<Task>() {
			@Override
			public String getVertexName(Task arg0) {
				return String.format("%d", arg0.hashCode());
			}
		};
		
		VertexNameProvider<Task> vertexNameProvider = new VertexNameProvider<Task>() {
			@Override
			public String getVertexName(Task arg0) {
				String name = new File(arg0.getCmd()).getName();
				String pid = String.format("%d", arg0.getProcessId());
				String str = name + " [" + pid + "]";
				return str;
			}
		};
		
		EdgeNameProvider<DefaultEdge> edgeNameProvider = new EdgeNameProvider<DefaultEdge>() {
			@Override
			public String getEdgeName(DefaultEdge arg0) {
				return "";
			}
		};
		
		ComponentAttributeProvider<Task> vertexAttributeProvider = new ComponentAttributeProvider<Task>() {
			@Override
			public Map<String, String> getComponentAttributes(Task arg0) {
				return new HashMap<String, String>();
			}
		};
		
		ComponentAttributeProvider<DefaultEdge> edgeAttributeProvider = new ComponentAttributeProvider<DefaultEdge>() {
			@Override
			public Map<String, String> getComponentAttributes(DefaultEdge arg0) {
				return new HashMap<String, String>();
			}
		};
		
		DOTExporter<Task, DefaultEdge> exporter = new DOTExporter<Task, DefaultEdge>(
				vertexIDProvider, vertexNameProvider, edgeNameProvider, 
				vertexAttributeProvider, edgeAttributeProvider);

		exporter.export(writer, graph);
	}
	
}
