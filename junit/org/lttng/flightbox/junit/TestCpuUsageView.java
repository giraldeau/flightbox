package org.lttng.flightbox.junit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.lttng.flightbox.TimeStatsBucket;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

/**
 * An example for line chart.
 */
public class TestCpuUsageView {

	/*
	private static final double[] ySeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
			0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };

	private static final double[] xSeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
		0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };
	 */
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Line Chart Example");
		shell.setSize(1200, 800);
		shell.setLayout(new FillLayout());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		shell.setLayout(layout);
		
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		
		createOverviewTab(tabFolder);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();

	}
	
	public Double getDutyCycle(Long x) {;
		return Math.sin(x);
	}
	
	public static TimeStatsBucket makeStatsBucket() {
		double start = 1;
		double end = 10;
		int precision = 5000;
		double intervals = 50;
		
		TimeStatsBucket stats = new TimeStatsBucket(start, end, precision);
		for(double i=10; i<intervals; i++) {
			double t1 = (i * 2) / intervals * (end - 2);
			//double t2 = (i * (Math.sin(i/intervals*12) + 1)) / intervals * end;
			double t2 = (i * 2 + 1) / intervals * (end - 2);
			stats.addInterval(t1, t2, KernelMode.USER);
		}
		return stats;
	}
	
	public static void createOverviewTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NULL);
		tab.setText("Overview");
		

		Group group = new Group(folder, SWT.NONE);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		TimeStatsBucket stats = makeStatsBucket();
		
		createChart(group, stats, "Duty function");
		//createChart(group, pwm, "PWM function");
		//createChart(group, avg100, "Average function 100ms");
		
		tab.setControl(group);
	}
	/*
	public static void addLineSeries(Chart chart, SampleSeries series, String title, Color color) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
			.createSeries(SeriesType.LINE, title);
		lineSeries.setXSeries(series.getXSeries());
		lineSeries.setYSeries(series.getYSeries());
		lineSeries.setAntialias(SWT.ON);
		lineSeries.enableArea(true);
		lineSeries.setLineColor(color);
	}*/
	
	public static Chart createChart(Composite parent, TimeStatsBucket series, String title) {
		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		
		// set titles
		chart.getTitle().setText(title);
		chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");
		
		// create line series
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, "line series");
		double[] dataX = new double[series.size()];
		double[] dataY = new double[series.size()];
		for(int i=0; i<series.size(); i++) {
			dataX[i] = series.getInterval(i).getStartTime();
		}
		for(int i=0; i<series.size(); i++) {
			dataY[i] = series.getInterval(i).getAvg(KernelMode.USER);
		}
		//lineSeries.setXSeries(series.getXSeries());
		//lineSeries.setYSeries(series.getYSeries());
		lineSeries.setXSeries(dataX);
		lineSeries.setYSeries(dataY);
		lineSeries.setAntialias(SWT.ON);
		lineSeries.enableArea(true);
		// adjust the axis range
		chart.getAxisSet().adjustRange();
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 1));
		return chart;
	}
	
}