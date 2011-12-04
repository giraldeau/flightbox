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
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Before;
import org.junit.Test;
import org.lttng.flightbox.graph.ExecEdge;
import org.lttng.flightbox.graph.ExecGraph;
import org.lttng.flightbox.graph.ExecGraphProviders;
import org.lttng.flightbox.graph.ExecSubgraph;
import org.lttng.flightbox.graph.ExecVertex;
import org.lttng.flightbox.graph.ReverseBreadthFirstIterator;
import org.lttng.flightbox.graph.ReverseClosestIterator;
import org.lttng.flightbox.graph.TaskGraphExtractor;
import org.lttng.flightbox.junit.Path;

public class TestExecutionGraphSimple {

	DirectedWeightedMultigraph<String, DefaultWeightedEdge> g1;
	ExecGraph g2;
	List<ExecVertex> g2v;
	String baseName = "EstExecutionGraphSimple-";
	
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
		
		for(int i=0; i<=14; i++) {
			ExecVertex v = new ExecVertex(i);
			v.setId(i);
			g2.addVertex(v);
			g2v.add(v);
		}
		/* master */
		g2.setEdgeWeight(g2.addEdge(g2v.get(0), g2v.get(1)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(1), g2v.get(2)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(2), g2v.get(3)), 2);
		g2.setEdgeWeight(g2.addEdge(g2v.get(3), g2v.get(4)), 1);
		/* subtask 1 */
		g2.setEdgeWeight(g2.addEdge(g2v.get(5), g2v.get(6)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(6), g2v.get(7)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(7), g2v.get(8)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(8), g2v.get(9)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(8), g2v.get(10)), 1);
		/* subtask 2 */
		g2.setEdgeWeight(g2.addEdge(g2v.get(11), g2v.get(12)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(12), g2v.get(13)), 1);
		g2.setEdgeWeight(g2.addEdge(g2v.get(13), g2v.get(14)), 1);
		
		/* relations */
		g2.setEdgeWeight(g2.addEdge(g2v.get(1), g2v.get(6)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(7), g2v.get(12)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(13), g2v.get(8)), 0);
		g2.setEdgeWeight(g2.addEdge(g2v.get(9), g2v.get(3)), 0);
		
	}

	@Test
	public void exportGraph() throws IOException {
		dumpExecGraph(g2, baseName + "g2");
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
		int[] exp = new int[] {0, 1, 6, 7, 12, 13, 8, 9, 3, 4};
		
		ReverseClosestIterator<ExecVertex, ExecEdge> iterator = new ReverseClosestIterator<ExecVertex, ExecEdge>(g2, g2v.get(4));
		ArrayList<ExecVertex> list = new ArrayList<ExecVertex>();
		while(iterator.hasNext()) {
			list.add(0, iterator.next());
		}
		//System.out.println(list + " " + Arrays.toString(exp) + "\n");
		for (int i=0; i<exp.length; i++) {
			assertEquals(exp[i], list.get(i).getId());
		}

	}
	
	@Test
	public void testTaskGraphExtractor() throws IOException {
		ExecSubgraph criticalPath = TaskGraphExtractor.getCriticalPath(g2, g2v.get(0), g2v.get(4));
		dumpExecGraph(criticalPath, baseName + "testTaskGraphExtractor");
		assertEquals(9, criticalPath.edgeSet().size());
		assertEquals(10, criticalPath.vertexSet().size());
	}
	
	/* get the whole connected execution graph */
	@Test
	public void testTaskGraphExtractor2() throws IOException {
		boolean[][] expCriticalMatrix = new boolean[15][15];
		expCriticalMatrix[0][1] = true;
		expCriticalMatrix[3][4] = true;
		expCriticalMatrix[6][7] = true;
		expCriticalMatrix[8][9] = true;
		expCriticalMatrix[12][13] = true;
		expCriticalMatrix[1][6] = true;
		expCriticalMatrix[7][12] = true;
		expCriticalMatrix[13][8] = true;
		expCriticalMatrix[9][3] = true;
		
		ExecSubgraph executionGraph = TaskGraphExtractor.getExecutionGraph(g2, g2v.get(0), g2v.get(4));
		dumpExecGraph(executionGraph, baseName + "testTaskGraphExtractor2");
		assertEquals(12, executionGraph.edgeSet().size());
		assertEquals(11, executionGraph.vertexSet().size());
		for (ExecEdge edge: executionGraph.edgeSet()) {
			int idsrc = g2.getEdgeSource(edge).getId();
			int iddst = g2.getEdgeTarget(edge).getId();
			//System.out.println(idsrc + " -> " + iddst + " isCritical=" + edge.isCriticalPath());
			assertEquals(expCriticalMatrix[idsrc][iddst], edge.isCriticalPath());
		}
	}
	
	static void dumpGraph(Graph<String, DefaultEdge> g, String out) throws IOException {
		VertexNameProvider<String> nameProvider = new VertexNameProvider<String>() {
			@Override
			public String getVertexName(String s) {
				return s;
			}
			
		};
		DOTExporter<String, DefaultEdge> dot = new DOTExporter<String, DefaultEdge>(nameProvider, null, null);
		StringWriter str = new StringWriter();
		dot.export(str, g);
		//System.out.println(str.toString());
		FileWriter fileWriter = new FileWriter(new File(Path.getGraphDir(), out + ".dot"));
		fileWriter.write(str.toString());
		fileWriter.flush();
	}
	
	static void dumpExecGraph(WeightedGraph<ExecVertex, ExecEdge> graph, String out) throws IOException {
		DOTExporter<ExecVertex, ExecEdge> dot = ExecGraphProviders.getDOTExporter();
		StringWriter str = new StringWriter();
		dot.export(str, graph);
		//System.out.println(str.toString());
		FileWriter fileWriter = new FileWriter(new File(Path.getGraphDir(), out + ".dot"));
		fileWriter.write(str.toString());
		fileWriter.flush();
	}
}
