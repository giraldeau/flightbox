package org.lttng.flightbox.junit;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.lttng.flightbox.GlobalState.KernelMode;
import org.lttng.flightbox.TimeStatsBucket;
import org.lttng.flightbox.UsageStats;
import org.lttng.flightbox.cpu.TraceEventHandlerStats;
import org.lttng.flightbox.io.EventQuery;
import org.lttng.flightbox.io.TraceReader;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
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
	 * @throws JniException 
	 */
	public static void main(String[] args) throws JniException {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Line Chart Example");
		shell.setSize(800, 800);
		shell.setLayout(new FillLayout());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		shell.setLayout(layout);
		
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		
		String tracePath = args[0];
		
		createCpuUsageTab(tabFolder, tracePath);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();

	}

	public static void createCpuUsageTab(TabFolder folder, String trace_path) throws JniException {
		TabItem tab = new TabItem(folder, SWT.NULL);
		tab.setText("Overview");
		

		Group group = new Group(folder, SWT.NONE);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		
		EventQuery sched_query = new EventQuery();
		sched_query.addEventType("kernel");
		sched_query.addEventName("sched_schedule");
		TraceEventHandlerStats cpu_handler = new TraceEventHandlerStats();
		TraceReader reader = new TraceReader(trace_path);
		reader.register(sched_query, cpu_handler);
		reader.process();
		
		System.out.println(cpu_handler.getCpuUsageStats());
		
		UsageStats<Long> cpuStats = cpu_handler.getCpuUsageStats();
		
		createChart(group, cpuStats, "Per CPU usage");
		createGlobalChart(group, cpuStats, "Global usage");
				
		tab.setControl(group);
	}
	
	
	public static void addLineSeries(Chart chart, String title, Color color, double[] dataX, double[] dataY) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
			.createSeries(SeriesType.LINE, title);
		lineSeries.setSymbolType(PlotSymbolType.NONE);
		lineSeries.setXSeries(dataX);
		lineSeries.setYSeries(dataY);
		lineSeries.setAntialias(SWT.ON);
		lineSeries.setLineColor(color);
	}
	
	public static Chart createChart(Composite parent, UsageStats<Long> stats, String title) {
		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		
		// set titles
		chart.getTitle().setText(title);
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time");
		chart.getAxisSet().getYAxis(0).getTitle().setText("CPU Usage");
		
		Color[] colors = new Color[10];
		colors[0] = new Color(Display.getDefault(), 255, 0, 0);
		colors[1] = new Color(Display.getDefault(), 0, 255, 0);
		colors[2] = new Color(Display.getDefault(), 0, 0, 255);
		colors[3] = new Color(Display.getDefault(), 255, 0, 255);
		colors[4] = new Color(Display.getDefault(), 255, 255, 0);
		colors[5] = new Color(Display.getDefault(), 0, 255, 255);
		colors[6] = new Color(Display.getDefault(), 126, 126, 0);
		colors[7] = new Color(Display.getDefault(), 0, 126, 126);
		
		for(int i=0; i<stats.getNumEntry(); i++) {
			double[] dataX = stats.getXSeries(new Long(i));
			double[] dataY = stats.getYSeries(new Long(i), KernelMode.USER);
			addLineSeries(chart, "CPU " + i, colors[i], dataX, dataY);
		}
		// adjust the axis range
		chart.getAxisSet().adjustRange();
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 1.1));
		return chart;
	}
	
	public static Chart createGlobalChart(Composite parent, UsageStats<Long> stats, String title) {
		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		
		// set titles
		chart.getTitle().setText(title);
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time");
		chart.getAxisSet().getYAxis(0).getTitle().setText("CPU Usage");
		
		Color color = new Color(Display.getDefault(), 255, 0, 0);

		TimeStatsBucket bucket = stats.getTotalAvg();
		
		double[] dataX = bucket.getXSeries();
		double[] dataY = bucket.getYSeries(KernelMode.USER);
		addLineSeries(chart, "Usage average", color, dataX, dataY);
		// adjust the axis range
		chart.getAxisSet().adjustRange();
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 1.1));
		return chart;
	}
}