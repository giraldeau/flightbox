package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.swtchart.ICustomPaintListener;

public class ChartHighlighter implements ICustomPaintListener {
	
	private int d1;
	private int d2;
	private Color fg;
	private int height;
	private int width;

	public ChartHighlighter() {
		Display display = Display.getCurrent();
	    fg = display.getSystemColor(SWT.COLOR_BLACK);
	    width = 1;
	}
	
	public void setInterval(int d1, int d2) {
		this.d1 = d1;
		this.d2 = d2;
	}
	
	public void setPixelInterval(int x1, int x2) {
		this.d1 = x1;
		this.d2 = x2;
	}
		
	@Override
	public void paintControl(PaintEvent e) {
		int oldAlpha = e.gc.getAlpha();
		e.gc.setAlpha(32);
		e.gc.setBackground(fg);
		e.gc.fillRectangle(d1, 0, d2 - d1, height);
		e.gc.setAlpha(oldAlpha);
	}

	@Override
	public boolean drawBehindSeries() {
		return false;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int w) {
		if (w <= 0)
			return;
		/* FIXME: int rounding shifts highlight to the left */
		d1 = w * d1 / width;
		d2 = w * d2 / width;
		width = w;
	}

}
