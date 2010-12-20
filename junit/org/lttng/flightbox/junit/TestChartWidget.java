package org.lttng.flightbox.junit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.lttng.flightbox.ui.ChartHighlighter;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;

public class TestChartWidget {

	private static final double[] ySeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
		0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };

	private static final double[] xSeries = {0, 1, 2, 3, 4, 5,
		6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	
	@Test
	public void testChartWidgetCreate() {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());
		Chart chart = new Chart(shell, 0);
		ChartHighlighter hl = new ChartHighlighter(chart, SWT.NONE);
		hl.setInterval(4.0, 6.0);
		
		// set titles
		chart.getTitle().setText("Line Chart Example");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");

		// create line series
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, "line series");
		lineSeries.setYSeries(ySeries);
		lineSeries.setXSeries(xSeries);

		// adjust the axis range
		chart.getAxisSet().adjustRange();
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		shell.dispose();
	}
}
