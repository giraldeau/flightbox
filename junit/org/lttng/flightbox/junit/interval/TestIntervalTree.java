package org.lttng.flightbox.junit.interval;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lttng.flightbox.interval.IntervalTree;

public class TestIntervalTree {

	@Test
	public void testCreateSimpleTree() {
		IntervalTree<Integer> tree = new IntervalTree<Integer>();
		tree.addInterval(0L, 10L, 1);
		tree.addInterval(20L, 30L, 2);
		tree.addInterval(15L, 17L, 3);
		tree.addInterval(25L, 35L, 4);
		
		List<Integer> a1 = Arrays.asList(1);
		List<Integer> r1 = tree.get(5L);
		assertTrue(a1.containsAll(r1));
		assertTrue(r1.containsAll(a1));

		List<Integer> a2 = Arrays.asList(1);
		List<Integer> r2 = tree.get(9L);
		assertTrue(a2.containsAll(r2));
		assertTrue(r2.containsAll(a2));
		
		List<Integer> a3 = Arrays.asList(2,4);
		List<Integer> r3 = tree.get(29L);
		assertTrue(a3.containsAll(r3));
		assertTrue(r3.containsAll(a3));
		
		List<Integer> a4 = Arrays.asList(1,3);
		List<Integer> r4 = tree.get(5L,16L);
		assertTrue(a4.containsAll(r4));
		assertTrue(r4.containsAll(a4));
	}
	
}
