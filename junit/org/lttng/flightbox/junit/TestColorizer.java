package org.lttng.flightbox.junit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.lttng.flightbox.ui.Colorizer;

public class TestColorizer {

	@Test
	public void testColorizer() {
		Colorizer colorizer = new Colorizer();
		RGB c1 = colorizer.getColor(new String("FOO"));
		RGB c2 = colorizer.getColor(new String("FOO"));
		assertEquals(c1, c2);
		assertSame(c1, c2);
		RGB c3 = colorizer.getColor(new String("BAR"));
		assertNotSame(c1, c3);
		assertFalse(c1.equals(c3));
	}

	@Test
	public void testAllColors() {
		Colorizer colorizer = new Colorizer();
		ArrayList<RGB> palette = colorizer.getPalette();
		Integer max = palette.size() * 2;
		
		HashSet<RGB> rgb = new HashSet<RGB>();
		
		for(Integer i = 0; i<max; i++) {
			RGB color = colorizer.getColor(i);
			if (rgb.contains(color)) {
				continue;
			} else {
				rgb.add(color); 
			}
		}
		
		assertEquals(palette.size(), rgb.size());
	}
}
