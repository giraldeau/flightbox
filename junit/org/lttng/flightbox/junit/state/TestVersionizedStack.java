package org.lttng.flightbox.junit.state;

import static org.junit.Assert.*;


import org.junit.Test;
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
	}
	
}

