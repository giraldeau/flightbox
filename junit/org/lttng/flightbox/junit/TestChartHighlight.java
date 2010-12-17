package org.lttng.flightbox.junit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.lttng.flightbox.ui.ChartWidget;

public class TestChartHighlight {

	@Test
	public void testChartHighlight() {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.None);
		shell.setLayout(new FillLayout());
		
		ChartWidget chart = new ChartWidget(shell, SWT.NONE);
		//chart.
	}
}
