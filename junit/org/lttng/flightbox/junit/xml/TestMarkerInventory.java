package org.lttng.flightbox.junit.xml;

import static org.junit.Assert.*;

import org.jdom.JDOMException;
import org.junit.Test;
import org.lttng.flightbox.xml.MarkerInventoryJDOM;

public class TestMarkerInventory {

	@Test
	public void testMarkerInventoryAddChannel() throws JDOMException {
		MarkerInventoryJDOM inventory = new MarkerInventoryJDOM();
		inventory.addChannel("foo");
		assertTrue(inventory.haveChannel("foo"));
	}
	
	@Test
	public void testMarkerInventoryAddEvent() throws JDOMException {
		MarkerInventoryJDOM inventory = new MarkerInventoryJDOM();
		inventory.addEvent("foo", "bar");
		assertTrue(inventory.haveEvent("foo", "bar"));
	}

	@Test
	public void testMarkerInventoryAddField() throws JDOMException {
		MarkerInventoryJDOM inventory = new MarkerInventoryJDOM();
		inventory.addField("foo", "bar", "baz");
		assertTrue(inventory.haveField("foo", "bar", "baz"));
	}
	
}
