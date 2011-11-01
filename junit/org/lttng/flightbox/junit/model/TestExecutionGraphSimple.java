package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Before;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraph;
import org.lttng.flightbox.graph.ExecSubgraph;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ReverseBreadthFirstIterator;
import org.lttng.flightbox.graph.ReverseClosestIterator;
import org.lttng.flightbox.graph.TaskGraphExtractor;

public class TestExecutionGraphSimple {

	DirectedWeightedMultigraph<String, DefaultWeightedEdge> g1;
	ExecGraph g2;
	List<ExecVertex> g2v;
	
	@Before
	public void setup() {
		g1 = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		g1.addVertex("s0");
		g1.addVertex("s1");
		g1.addVertex("s2");
		g1.addVertex("s3");
		g1.addVertex("s4");
		g1.addVertex("s5");
		g1.addVertex("s6");
		g1.addVertex("s7");
		g1.addVertex("s8");
		g1.addVertex("s9");
		g1.addVertex("s10");
		
		g1.setEdgeWeight(g1.addEdge("s0", "s1"), 1);
		g1.setEdgeWeight(g1.addEdge("s1", "s2"), 1);
		g1.setEdgeWeight(g1.addEdge("s2", "s3"), 1);
		g1.setEdgeWeight(g1.addEdge("s3", "s4"), 1);
		g1.setEdgeWeight(g1.addEdge("s4", "s5"), 1);
		g1.setEdgeWeight(g1.addEdge("s1", "s6"), 0);
		g1.setEdgeWeight(g1.addEdge("s6", "s7"), 1);
		g1.setEdgeWeight(g1.addEdge("s7", "s8"), 1);
		g1.setEdgeWeight(g1.addEdge("s8", "s4"), 0);
		g1.setEdgeWeight(g1.addEdge("s4", "s9"), 0);
		g1.setEdgeWeight(g1.addEdge("s9", "s10"), 1);
		
		g2 = new ExecGraph();
		g2v = new ArrayList<ExecVertex>();
		
		for(int i=0; i<=10; i++) {
			ExecVertex v = new ExecVertex(i);
			g2.addVertex(v);
			g2v.add(v);
		}
		g2.setEdgeWeight(g2.addEdge(g2v.get(0), g2v.get(1)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(1), g2v.get(2)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(2), g2v.get(3)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(3), g2v.get(4)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(4), g2v.get(5)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(1), g2v.get(6)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(6), g2v.get(7)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(7), g2v.get(8)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(8), g2v.get(4)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(4), g2v.get(9)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(9), g2v.get(10)), 1);
	}
	
	@Test
	public void testReverseBreadthFirstIterator() throws IOException {
		//dumpGraph(g1);
		List<String> exp = Arrays.asList("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8");
		ReverseBreadthFirstIterator<String, DefaultWeightedEdge> iterator = new ReverseBreadthFirstIterator<String, DefaultWeightedEdge>(g1, "s5");
		ArrayList<String> list = new ArrayList<String>();
		while(iterator.hasNext()) {
			list.add(0, iterator.next());
		}
		//System.out.println(list + " " + exp);
		assertEquals(9, list.size());
		assertTrue(list.containsAll(exp));
		assertTrue(exp.containsAll(list));
	}
	
	@Test
	public void testReverseClosestIterator() {
		List<String> exp = Arrays.asList("s0", "s1", "s6", "s7", "s8", "s4", "s5");
		ReverseClosestIterator<String, DefaultWeightedEdge> iterator = new ReverseClosestIterator<String, DefaultWeightedEdge>(g1, "s5");
		ArrayList<String> list = new ArrayList<String>();
		while(iterator.hasNext()) {
			String next = iterator.next();
			list.add(0, next);
		}
		for (int i=0; i<exp.size();i++) {
			assertEquals(exp.get(i), list.get(i));
		}
		//System.out.println(list + " " + exp);
	}
	
	@Test
	public void testRecoverExecGraphSimple() {
		List<ExecVertex> exp = Arrays.asList(g2v.get(0), g2v.get(1), g2v.get(6), g2v.get(7), g2v.get(8), g2v.get(4), g2v.get(5));
		ReverseClosestIterator<ExecVertex, ExecEdge> iterator = new ReverseClosestIterator<ExecVertex, ExecEdge>(g2, g2v.get(5));
		ArrayList<ExecVertex> list = new ArrayList<ExecVertex>();
		while(iterator.hasNext()) {
			list.add(0, iterator.next());
		}
		for (int i=0; i<exp.size();i++) {
			assertEquals(exp.get(i), list.get(i));
		}
		//System.out.println(list + " " + exp);
	}
	
	@Test
	public void testTaskGraphExtractor() throws IOException {
		ExecSubgraph criticalPath = TaskGraphExtractor.getCriticalPathAnnotate(g2, g2v.get(0), g2v.get(5), true);
		assertEquals(6, criticalPath.edgeSet().size());
		assertEquals(7, criticalPath.vertexSet().size());
		for (ExecEdge edge: criticalPath.edgeSet()) {
			assertTrue(edge.isCriticalPath());
		}
		//dumpExecGraph(criticalPath);
	}
	
	static void dumpGraph(Graph<String, DefaultEdge> g) throws IOException {
		VertexNameProvider<String> nameProvider = new VertexNameProvider<String>() {
			@Override
			public String getVertexName(String s) {
				return s;
			}
			
		};
		DOTExporter<String, DefaultEdge> dot = new DOTExporter<String, DefaultEdge>(nameProvider, null, null);
		StringWriter str = new StringWriter();
		dot.export(str, g);
		System.out.println(str.toString());
		FileWriter fileWriter = new FileWriter(new File("out.dot"));
		fileWriter.write(str.toString());
		fileWriter.flush();
	}
	
	static void dumpExecGraph(ExecSubgraph graph) throws IOException {
		VertexNameProvider<ExecVertex> nameProvider = new VertexNameProvider<ExecVertex>() {
			@Override
			public String getVertexName(ExecVertex v) {
				return String.format("%d", v.getId());
			}
			
		};
		DOTExporter<ExecVertex, ExecEdge> dot = new DOTExporter<ExecVertex, ExecEdge>(nameProvider, null, null);
		StringWriter str = new StringWriter();
		dot.export(str, graph);
		System.out.println(str.toString());
		FileWriter fileWriter = new FileWriter(new File("out.dot"));
		fileWriter.write(str.toString());
		fileWriter.flush();
	}
	
}
