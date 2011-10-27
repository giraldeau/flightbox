package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;
import org.lttng.flightbox.graph.ReverseBreadthFirstIterator;

public class TestExecutionGraphSimple {

	DirectedGraph<String, DefaultEdge> g1;
	
	@Before
	public void setup() {
		g1 = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);
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
		
		g1.addEdge("s0", "s1");
		g1.addEdge("s1", "s2");
		g1.addEdge("s2", "s3");
		g1.addEdge("s3", "s4");
		g1.addEdge("s4", "s5");
		g1.addEdge("s1", "s6");
		g1.addEdge("s6", "s7");
		g1.addEdge("s7", "s8");
		g1.addEdge("s8", "s4");
		g1.addEdge("s4", "s9");
		g1.addEdge("s9", "s10");
	}
	
	@Test
	public void testSetup() throws IOException {
		dumpGraph(g1);
		ReverseBreadthFirstIterator<String, DefaultEdge> iterator = new ReverseBreadthFirstIterator<String, DefaultEdge>(g1, "s5");
		ArrayList<String> list = new ArrayList<String>();
		while(iterator.hasNext()) {
			list.add(0, iterator.next());
		}
		System.out.print(list.toString());
		assertEquals(9, list.size());
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
	
}
