package org.lttng.flightbox.ui;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.lttng.flightbox.state.VersionizedStack;

public class TestIntervalWidget {

	/*
	 * We need a running X server for this test to run which is ugly
	 * and may not run on automated test servers 
	 */
	@Test
	public void testIntervalWidgetBasic() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		int size = 100;
		shell.setSize(size, size);
		shell.setLayout(new FillLayout());
		IntervalWidget widget = new IntervalWidget(shell, SWT.NONE);
		
		VersionizedStack<String> stack = new VersionizedStack<String>();
		stack.push("FOO", 10L);
		stack.push("BAR", 20L);
		stack.pop(30L);
		stack.pop(40L);
		
		widget.setStack(stack);
		shell.open();
		
		boolean run = true;
		Image image = null;
		while(run) {
			if (!display.readAndDispatch()) {
				image = widget.getImage();
				run = false;
			}
		}
		
		assertNotNull(image);
		widget.save("test.png");
		ImageData img = image.getImageData();
		assertEquals(23, img.height);
		int center = img.height/2;
		assertEquals(size - (2*3), img.width);
		int p1 = img.getPixel(widget.getPixedCoordinate(11L), center);
		int p2 = img.getPixel(widget.getPixedCoordinate(39L), center);
		int p3 = img.getPixel(widget.getPixedCoordinate(21L), center);
		int p4 = img.getPixel(widget.getPixedCoordinate(29L), center);
		
		assertEquals(p1, p2);
		assertEquals(p3, p4);
		assertTrue(p1 != p3);
		
		
		shell.dispose();
		
	}
	
}
