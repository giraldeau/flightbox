package org.lttng.flightbox.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.swtchart.Chart;

public class ChartWidget extends Chart {

	PlotAreaHighlighter highlighter;

	public ChartWidget(Composite parent, int style) {
		super(parent, style);
		highlighter = new PlotAreaHighlighter(this, SWT.NONE);
		Rectangle rect = this.getBounds();
		addListener(SWT.Resize, this);
	}

	public void handleEvent(Event event) {
		Rectangle rect;
		switch (event.type) {
		case SWT.Resize:
			updateLayout();
			redraw();
			rect = getBounds();
			highlighter.setBounds(rect);
			highlighter.setInterval(200, 210);
			highlighter.redraw();
			break;
		default:
			break;
		}

	}

	public void highlightInterval(int x1, int x2) {
		highlighter.setInterval(x1, x2);
		redraw();
	}

}
