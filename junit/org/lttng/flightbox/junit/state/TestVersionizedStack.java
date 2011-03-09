package org.lttng.flightbox.junit.state;

import static org.junit.Assert.*;

import java.util.Iterator;


import org.junit.Test;
import org.lttng.flightbox.state.Interval;
import org.lttng.flightbox.state.VersionizedStack;

public class TestVersionizedStack {

	@Test
	public void testVersionizedStackPush() {
		VersionizedStack<String> stack;
		
		stack = new VersionizedStack<String>();		
		assertEquals(null, stack.peek(5L));
		
		stack = new VersionizedStack<String>();
		stack.push("foo", 1L);
		assertEquals(null, stack.peek(0L));
		assertEquals("foo", stack.peek(1L));
		assertEquals("foo", stack.peek(5L));
		
		stack = new VersionizedStack<String>();
		stack.push("foo", 1L);
		stack.push("bar", 10L);
		assertEquals(null, stack.peek(0L));
		assertEquals("foo", stack.peek(5L));
		assertEquals("bar", stack.peek(10L));
		assertEquals("bar", stack.peek(15L));
		
		stack = new VersionizedStack<String>();
		stack.push("foo", 1L);
		stack.push("bar", 10L);
		stack.push("baz", 15L);
		assertEquals(null, stack.peek(0L));
		assertEquals("foo", stack.peek(5L));
		assertEquals("bar", stack.peek(10L));
		assertEquals("bar", stack.peek(12L));
		assertEquals("baz", stack.peek(15L));
		assertEquals("baz", stack.peek(16L));
	}
	
	@Test
	public void testVersionStackPop() {
		VersionizedStack<String> stack;
		stack = new VersionizedStack<String>();
		stack.push("foo", 1L);
		stack.push("bar", 10L);
		stack.push("baz", 15L);
		stack.pop(20L);
		stack.pop(25L);
		stack.pop(30L);
		assertEquals(null, stack.peek(0L));
		assertEquals("foo", stack.peek(5L));
		assertEquals("bar", stack.peek(10L));
		assertEquals("bar", stack.peek(12L));
		assertEquals("baz", stack.peek(15L));
		assertEquals("bar", stack.peek(20L));
		assertEquals("foo", stack.peek(26L));
		assertEquals(null, stack.peek(30L));
		assertEquals("foo", stack.peekInclusive(30L));
	}
	
	@Test
	public void testVersionStackIntervalIterator() {
		VersionizedStack<String> stack;
		stack = new VersionizedStack<String>();
		stack.push("foo", 1L);
		stack.push("bar", 10L);
		stack.push("baz", 15L);
		stack.pop(20L);
		stack.pop(25L);
		stack.pop(30L);
		
		int i = 0;
		Interval prev = null;
		for (Iterator<Interval> iter = stack.iterator(); iter.hasNext();) {
			Interval curr = iter.next();
			if (prev != null) {
				assertEquals(prev.t2, curr.t1);
			}
			i++;
		}
		
		assertEquals(5, i);
	}
	
	@Test
	public void testVersionStackFixInitialState() {
		VersionizedStack<String> stack;
		stack = new VersionizedStack<String>();
		stack.push("bar", 10L);
		stack.pop("bar", 25L);
		stack.pop("foo", 30L);
		assertEquals("foo", stack.peek(1L));
		assertEquals("bar", stack.peek(10L));
		assertEquals("foo", stack.peek(29L));
		assertEquals(null, stack.peek(30L));
		assertEquals(4, stack.getHistory().size());
	}
	
	@Test
	public void testVersionStackFixInitialState2() {
		VersionizedStack<String> stack;
		stack = new VersionizedStack<String>();
		stack.push("bar", 5L);
		stack.pop("bar", 10L);
		stack.push("bar", 15L);
		stack.pop("bar", 20L);
		stack.pop("foo", 25L);
		stack.pop("baz", 30L);
		assertEquals("foo", stack.peek(1L));
		assertEquals("bar", stack.peek(6L));
		assertEquals("foo", stack.peek(11L));
		assertEquals("bar", stack.peek(16L));
		assertEquals("foo", stack.peek(21L));
		assertEquals("baz", stack.peek(26L));
		assertEquals(null, stack.peek(31L));
		assertEquals(7, stack.getHistory().size());
	}
}

